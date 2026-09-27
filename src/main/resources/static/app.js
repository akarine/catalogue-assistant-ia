'use strict';

const API = '/api';

// ---------- Utilitaires ----------

const escapeHtml = (text) => String(text ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
}[c]));

const formatPrice = (value) => Number(value).toLocaleString('fr-FR', {style: 'currency', currency: 'EUR'});

/** Mise en forme minimale des réponses de l'IA (gras, listes, retours à la ligne), après échappement. */
function renderMarkdown(text) {
    const lines = escapeHtml(text).replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>').split('\n');
    let html = '';
    let inList = false;
    for (const line of lines) {
        const item = line.match(/^\s*[-*]\s+(.*)$/);
        if (item) {
            if (!inList) { html += '<ul>'; inList = true; }
            html += `<li>${item[1]}</li>`;
        } else {
            if (inList) { html += '</ul>'; inList = false; }
            if (line.trim()) html += `<p>${line}</p>`;
        }
    }
    return inList ? html + '</ul>' : html;
}

/** Appel à l'API ; les erreurs Problem Details (RFC 9457) sont remontées avec leur message. */
async function callApi(path, options) {
    const response = await fetch(API + path, options);
    const body = await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(body?.detail || body?.title || `Erreur ${response.status}`);
    }
    return body;
}

// ---------- Onglets ----------

document.querySelectorAll('.tab').forEach((tab) => tab.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach((t) => {
        t.classList.toggle('is-active', t === tab);
        t.setAttribute('aria-selected', String(t === tab));
    });
    document.querySelectorAll('.panel').forEach((p) => p.classList.toggle('is-active', p.id === tab.dataset.panel));
    if (tab.dataset.panel === 'assistant') document.getElementById('question').focus();
}));

// ---------- Catalogue ----------

const filters = document.getElementById('filters');
const productsEl = document.getElementById('products');
const countEl = document.getElementById('results-count');

function productCard(p) {
    const available = p.stock > 0;
    return `
        <article class="card">
            <span class="card__category">${escapeHtml(p.category)} · ${escapeHtml(p.brand)}</span>
            <h3 class="card__name">${escapeHtml(p.name)}</h3>
            <p class="card__desc">${escapeHtml(p.description)}</p>
            <div class="card__footer">
                <span class="price">${formatPrice(p.price)}</span>
                <span class="badge ${available ? 'badge--ok' : 'badge--ko'}">
                    ${available ? `En stock (${p.stock})` : 'Rupture'}
                </span>
            </div>
        </article>`;
}

async function loadProducts() {
    const params = new URLSearchParams();
    for (const [key, value] of new FormData(filters)) {
        if (String(value).trim()) params.set(key, String(value).trim());
    }
    try {
        const products = await callApi(`/products?${params}`);
        countEl.textContent = `${products.length} produit${products.length > 1 ? 's' : ''}`;
        productsEl.innerHTML = products.map(productCard).join('');
    } catch (e) {
        countEl.textContent = `Impossible de charger le catalogue : ${e.message}`;
        productsEl.innerHTML = '';
    }
}

async function loadCategories() {
    try {
        const select = filters.elements.category;
        for (const category of await callApi('/products/categories')) {
            select.add(new Option(category, category));
        }
    } catch (e) {
        console.warn('Catégories indisponibles', e);
    }
}

filters.addEventListener('submit', (event) => {
    event.preventDefault();
    loadProducts();
});
filters.elements.category.addEventListener('change', loadProducts);

// ---------- Assistant IA ----------

const messagesEl = document.getElementById('messages');
const chatForm = document.getElementById('chat-form');
const questionEl = document.getElementById('question');

function addMessage(html, type) {
    const div = document.createElement('div');
    div.className = `msg msg--${type}`;
    div.innerHTML = html;
    messagesEl.appendChild(div);
    messagesEl.scrollTop = messagesEl.scrollHeight;
    return div;
}

async function ask(question) {
    addMessage(escapeHtml(question), 'user');
    const typing = addMessage('L\'assistant consulte le catalogue…', 'bot msg--typing');
    const button = chatForm.querySelector('button');
    button.disabled = true;
    try {
        const {answer} = await callApi('/assistant/chat', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({question})
        });
        typing.remove();
        addMessage(renderMarkdown(answer), 'bot');
    } catch (e) {
        typing.remove();
        const hint = /429|quota|rate/i.test(e.message)
            ? ' Le quota gratuit du fournisseur d\'IA est atteint : réessayez dans une minute.'
            : '';
        addMessage(escapeHtml(e.message.replace(/\.?$/, '.')) + hint, 'error');
    } finally {
        button.disabled = false;
        questionEl.focus();
    }
}

chatForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const question = questionEl.value.trim();
    if (!question) return;
    questionEl.value = '';
    ask(question);
});

document.querySelectorAll('.chip').forEach((chip) => chip.addEventListener('click', () => ask(chip.textContent)));

// ---------- Démarrage ----------

loadCategories();
loadProducts();
