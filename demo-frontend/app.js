// Demo frontend for restpartijen-api. One plain page, no build, no framework.
// Every button does exactly one HTTP call and shows the request and the response in the panel on the right.

'use strict';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DAY_LABELS = ['maandag', 'dinsdag', 'woensdag', 'donderdag', 'vrijdag', 'zaterdag', 'zondag'];

// Tokens per role, so the flow logs in once per role and later steps reuse the right token.
const state = {
  baseUrl: 'http://localhost:8080/api/v1',
  tokens: {},            // role -> JWT
  supplierId: null,      // of the supplier that logged in last
  email: null,
};

const $ = (selector) => document.querySelector(selector);
const form = (id) => document.getElementById(id);
const fieldsOf = (f) => Object.fromEntries(new FormData(f).entries());

// ---------------------------------------------------------------------------------------------
// The API call: builds the request, shows it, sends it, shows the response. Returns { status, data }.

async function api(method, path, { body, role } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (role) {
    const token = state.tokens[role];
    // Without a token the call still goes out, so the 401 of the API is visible in the demo.
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  showRequest(method, path, headers, body, role);
  let response;
  try {
    response = await fetch(state.baseUrl + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (error) {
    showResponse(null, `Geen verbinding met ${state.baseUrl}: ${error.message}. Draait de API?`);
    return { status: 0, data: null };
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = text; }
  }
  showResponse(response.status, data);
  return { status: response.status, data };
}

function showRequest(method, path, headers, body, role) {
  $('#call-line').textContent = `${method} ${path}`;
  const lines = Object.entries(headers).map(([name, value]) => {
    // Keep the token short on screen; the full token is sent.
    const shown = name === 'Authorization' ? value.slice(0, 24) + '…' : value;
    return `${name}: ${shown}`;
  });
  if (role && !state.tokens[role]) lines.push(`(geen token voor rol ${role}: eerst inloggen)`);
  if (body !== undefined) lines.push('', JSON.stringify(body, null, 2));
  $('#call-request').textContent = lines.join('\n');
  $('#call-status').textContent = 'bezig…';
  $('#call-status').className = 'call-status';
  $('#call-response').textContent = '';
}

function showResponse(status, data) {
  const statusLine = $('#call-status');
  if (status === null) {
    statusLine.textContent = 'Fout';
    statusLine.className = 'call-status error';
    $('#call-response').textContent = data;
    return;
  }
  statusLine.textContent = `${status} ${STATUS_TEXT[status] ?? ''}`.trim();
  statusLine.className = 'call-status ' + (status < 400 ? 'ok' : 'error');
  $('#call-response').textContent = data === null ? '(geen body)' : JSON.stringify(data, null, 2);
}

const STATUS_TEXT = {
  200: 'OK', 201: 'Created', 204: 'No Content', 400: 'Bad Request', 401: 'Unauthorized',
  403: 'Forbidden', 404: 'Not Found', 405: 'Method Not Allowed', 409: 'Conflict', 422: 'Unprocessable Entity',
  500: 'Internal Server Error',
};

// ---------------------------------------------------------------------------------------------
// Small helpers for the forms.

// "MILK, NUTS" -> ["MILK", "NUTS"]; empty stays empty, so the API gets a valid set.
function parseAllergens(text) {
  return text.split(',').map((s) => s.trim().toUpperCase()).filter(Boolean);
}

// ISO date (yyyy-mm-dd) a number of days from today, as the API wants bestBeforeAt.
function dateInDays(days) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString().slice(0, 10);
}

// A number field that may be left empty, like the ids that are filled in by an earlier step.
function requireId(value, label) {
  if (!value) {
    alert(`Vul eerst ${label} in (of doe eerst de stap die het id oplevert).`);
    return null;
  }
  return value;
}

// Disables the buttons of a form while its call runs, so one click is one call.
// Also puts the heading of the step (the user story) above the call panel, so the video shows which story the call belongs to.
async function busy(f, work) {
  $('#call-story').textContent = f.closest('.step').querySelector('h2').textContent.trim();
  const buttons = f.querySelectorAll('button');
  buttons.forEach((b) => (b.disabled = true));
  try { await work(); } finally { buttons.forEach((b) => (b.disabled = false)); }
}

function onSubmit(id, handler) {
  const f = form(id);
  f.addEventListener('submit', (event) => {
    event.preventDefault();
    busy(f, () => handler(fieldsOf(f), f));
  });
  return f;
}

function onAction(f, action, handler) {
  f.querySelector(`[data-action="${action}"]`).addEventListener('click', () => busy(f, () => handler(fieldsOf(f), f)));
}

// ---------------------------------------------------------------------------------------------
// Start: login (GI-3).

onSubmit('form-login', async (fields) => {
  state.baseUrl = fields.baseUrl.replace(/\/$/, '');
  const { status, data } = await api('POST', '/auth/login', { body: { email: fields.email, password: fields.password } });
  if (status !== 200) {
    $('#login-status').textContent = 'Inloggen mislukt, zie de response.';
    return;
  }
  state.tokens[data.role] = data.token;
  state.email = fields.email;
  // Only a supplier login carries a supplierId (FTD §10.1); the other roles get null.
  state.supplierId = data.supplierId ?? null;
  const known = Object.keys(state.tokens).join(', ');
  $('#login-status').textContent = `Ingelogd als ${fields.email} (${data.role}). Tokens in geheugen voor: ${known}.`;
});

// ---------------------------------------------------------------------------------------------
// US-10: opening hours per weekday. Empty times mean closed; the PUT replaces the whole week.

(function buildHoursTable() {
  const rows = DAYS.map((day, i) => `
    <tr>
      <th>${DAY_LABELS[i]}</th>
      <td><input type="time" name="${day}-open" step="60"></td>
      <td><input type="time" name="${day}-close" step="60"></td>
    </tr>`);
  $('#hours-table').innerHTML = '<tr><th></th><th>open</th><th>dicht</th></tr>' + rows.join('');
  // A sensible default week, so the video does not start with an empty form.
  DAYS.slice(0, 6).forEach((day) => {
    form('form-opening-hours').elements[`${day}-open`].value = '08:00';
    form('form-opening-hours').elements[`${day}-close`].value = '18:00';
  });
})();

function supplierPath(suffix) {
  const id = state.supplierId ?? 1; // without a supplier login the demo shows supplier 1
  return `/suppliers/${id}${suffix}`;
}

const hoursForm = onSubmit('form-opening-hours', async (fields) => {
  const week = DAYS
    .filter((day) => fields[`${day}-open`] || fields[`${day}-close`])
    .map((day) => ({ day, opensAt: fields[`${day}-open`], closesAt: fields[`${day}-close`] }));
  await api('PUT', supplierPath('/opening-hours'), { body: week, role: 'SUPPLIER' });
});

onAction(hoursForm, 'get-hours', async () => {
  const { status, data } = await api('GET', supplierPath('/opening-hours'));
  if (status !== 200) return;
  DAYS.forEach((day) => {
    const entry = data.find((h) => h.day === day);
    hoursForm.elements[`${day}-open`].value = entry ? entry.opensAt : '';
    hoursForm.elements[`${day}-close`].value = entry ? entry.closesAt : '';
  });
});

// ---------------------------------------------------------------------------------------------
// US-02: barcode lookup. The result flows into the US-01 form.

onSubmit('form-lookup', async (fields) => {
  const { status, data } = await api('GET', `/products/lookup/${encodeURIComponent(fields.barcode)}`, { role: 'SUPPLIER' });
  if (status !== 200) return;
  const place = form('form-place');
  if (data.name) place.elements.name.value = data.name;
  place.elements.allergens.value = data.allergens.join(', ');
  place.elements.barcode.value = fields.barcode;
});

// ---------------------------------------------------------------------------------------------
// US-01: place a product. The new id flows into US-03, US-07 and US-05.

form('form-place').elements.bestBeforeAt.value = dateInDays(5);
form('form-manage').elements.bestBeforeAt.value = dateInDays(5);

onSubmit('form-place', async (fields) => {
  const body = {
    category: fields.category,
    name: fields.name,
    originalPrice: Number(fields.originalPrice),
    bestBeforeAt: fields.bestBeforeAt,
    quantity: Number(fields.quantity),
    allergens: parseAllergens(fields.allergens),
  };
  if (fields.barcode) body.barcode = fields.barcode;
  const { status, data } = await api('POST', '/products', { body, role: 'SUPPLIER' });
  if (status !== 201) return;
  form('form-manage').elements.productId.value = data.id;
  form('form-manage').elements.barcode.value = data.barcode ?? '';
  form('form-manage').elements.allergens.value = data.allergens.join(', ');
  form('form-price').elements.productId.value = data.id;
  form('form-reserve').elements.productId.value = data.id;
});

// ---------------------------------------------------------------------------------------------
// US-03: my offer, update, delete.

const manageForm = onSubmit('form-manage', async (fields) => {
  const id = requireId(fields.productId, 'het partij-id');
  if (!id) return;
  const body = {
    name: fields.name,
    originalPrice: Number(fields.originalPrice),
    bestBeforeAt: fields.bestBeforeAt,
    quantity: Number(fields.quantity),
    allergens: parseAllergens(fields.allergens),
  };
  // PUT replaces every field, so a barcode left out would become null.
  if (fields.barcode) body.barcode = fields.barcode;
  await api('PUT', `/products/${id}`, { body, role: 'SUPPLIER' });
});

onAction(manageForm, 'my-products', () => api('GET', supplierPath('/products')));

onAction(manageForm, 'delete-product', async (fields) => {
  const id = requireId(fields.productId, 'het partij-id');
  if (id) await api('DELETE', `/products/${id}`, { role: 'SUPPLIER' });
});

// ---------------------------------------------------------------------------------------------
// US-07: the price is part of the product.

onSubmit('form-price', (fields) => api('GET', `/products/${fields.productId}`));

// ---------------------------------------------------------------------------------------------
// US-04: search. Only filled-in filters go into the query string.

onSubmit('form-search', async (fields) => {
  const query = new URLSearchParams();
  for (const name of ['category', 'maxPrice', 'minShelfLifeHours']) {
    if (fields[name]) query.set(name, fields[name]);
  }
  const suffix = query.toString() ? `?${query}` : '';
  await api('GET', `/products${suffix}`);
});

// ---------------------------------------------------------------------------------------------
// US-05: reserve and list own reservations. The reservation id flows into US-06 and US-11.

const reserveForm = onSubmit('form-reserve', async (fields) => {
  const { status, data } = await api('POST', '/reservations', { body: { productId: Number(fields.productId) }, role: 'COLLECTOR' });
  if (status !== 201) return;
  form('form-cancel').elements.reservationId.value = data.id;
  form('form-collect').elements.reservationId.value = data.id;
});

onAction(reserveForm, 'my-reservations', () => api('GET', '/reservations', { role: 'COLLECTOR' }));

// US-06 and US-11 follow FTD §10.1; see README.md for the assumed responses.
onSubmit('form-cancel', async (fields) => {
  const id = requireId(fields.reservationId, 'het reservering-id');
  if (id) await api('DELETE', `/reservations/${id}`, { role: 'COLLECTOR' });
});

onSubmit('form-collect', async (fields) => {
  const id = requireId(fields.reservationId, 'het reservering-id');
  if (id) await api('POST', `/reservations/${id}/collect`, { role: 'COLLECTOR' });
});

// ---------------------------------------------------------------------------------------------
// US-08 and US-09: admin.

onSubmit('form-maintenance', () => api('POST', '/admin/maintenance/expire', { role: 'ADMIN' }));

const adminForm = onSubmit('form-admin', async (fields, f) => {
  // The status filter is a repeated parameter (?status=LISTED&status=RESERVED), not comma separated (B-26).
  const query = new URLSearchParams();
  f.querySelectorAll('input[name="status"]:checked').forEach((box) => query.append('status', box.value));
  const suffix = query.toString() ? `?${query}` : '';
  await api('GET', `/admin/products${suffix}`, { role: 'ADMIN' });
});

onAction(adminForm, 'admin-delete', async (fields) => {
  const id = requireId(fields.productId, 'het partij-id');
  if (id) await api('DELETE', `/admin/products/${id}`, { role: 'ADMIN' });
});
