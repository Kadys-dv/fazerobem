import { createHmac } from 'node:crypto';
import { request, test, expect, APIRequestContext } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const MEMBER_COUNT = Number(process.env.PILOT_MEMBER_COUNT || 50);
const AID_COUNT = Number(process.env.PILOT_AID_COUNT || 20);
const PAID_SAMPLE_COUNT = Number(process.env.PILOT_PAID_SAMPLE_COUNT || 3);
const CONCURRENCY = Number(process.env.PILOT_CONCURRENCY || 10);
const MEMBER_PASSWORD = 'PilotOnly123!';
const DEMO_PASSWORD = 'Demo12345!';
const WEBHOOK_SECRET = process.env.SANDBOX_WEBHOOK_SECRET || '';

type Csrf = { headerName: string; parameterName: string; token: string };
type AidSeed = { id: string; email: string; memberId: string };

async function csrf(ctx: APIRequestContext): Promise<Csrf> {
  const response = await ctx.get('/api/v1/auth/csrf');
  expect(response.ok(), `csrf failed: ${response.status()} ${await response.text()}`).toBeTruthy();
  return response.json();
}

async function jsonMutation(ctx: APIRequestContext, path: string, data: unknown, headers: Record<string, string> = {}) {
  const token = await csrf(ctx);
  return ctx.post(path, {
    headers: { [token.headerName]: token.token, 'Content-Type': 'application/json', ...headers },
    data
  });
}

async function login(ctx: APIRequestContext, email: string, password: string) {
  const token = await csrf(ctx);
  const response = await ctx.post('/login', {
    form: { username: email, password, [token.parameterName]: token.token }
  });
  expect(response.ok(), `login failed for ${email}: ${response.status()}`).toBeTruthy();
  const me = await ctx.get('/api/v1/auth/me');
  expect(me.ok()).toBeTruthy();
  const body = await me.json();
  expect(body.authenticated).toBe(true);
  expect(body.email).toBe(email);
  return body;
}

async function operator(email: string) {
  const ctx = await request.newContext({ baseURL: BASE_URL });
  await login(ctx, email, DEMO_PASSWORD);
  return ctx;
}

async function seedMember(index: number, runId: string): Promise<AidSeed | null> {
  const ctx = await request.newContext({ baseURL: BASE_URL });
  try {
    const email = `pilot-${runId}-${String(index).padStart(3, '0')}@example.test`;
    const register = await jsonMutation(ctx, '/api/v1/auth/register', {
      name: `Membro Piloto ${index}`,
      email,
      password: MEMBER_PASSWORD
    });
    expect(register.status(), `register ${index}: ${await register.text()}`).toBe(201);

    const me = await login(ctx, email, MEMBER_PASSWORD);
    const memberId = me.memberId as string;

    const contribution = await jsonMutation(ctx, '/api/v1/contributions', { memberId, amount: '25.00' });
    expect(contribution.status(), `contribution ${index}: ${await contribution.text()}`).toBe(201);

    if (index >= AID_COUNT) return null;

    const aid = await jsonMutation(ctx, '/api/v1/aid-requests', {
      memberId,
      amount: '10.00',
      category: 'HEALTH',
      reason: `Simulação de piloto ${runId} membro ${index}`,
      emergency: false
    });
    expect(aid.status(), `aid ${index}: ${await aid.text()}`).toBe(201);
    const created = await aid.json();

    const token = await csrf(ctx);
    const document = await ctx.post(`/api/v1/aid-requests/${created.id}/documents`, {
      headers: { [token.headerName]: token.token },
      multipart: {
        documentType: 'LAUDO_OU_RECEITA',
        file: {
          name: `pilot-${index}.png`,
          mimeType: 'image/png',
          buffer: Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x00])
        }
      }
    });
    expect(document.status(), `document ${index}: ${await document.text()}`).toBe(201);

    return { id: created.id, email, memberId };
  } finally {
    await ctx.dispose();
  }
}

async function inBatches<T>(items: T[], size: number, worker: (item: T) => Promise<void>) {
  for (let i = 0; i < items.length; i += size) {
    await Promise.all(items.slice(i, i + size).map(worker));
  }
}

test.describe.configure({ mode: 'serial', retries: 0 });

test('phase 8 pilot simulation preserves governance and financial invariants', async () => {
  test.setTimeout(180_000);
  expect(MEMBER_COUNT).toBeGreaterThanOrEqual(10);
  expect(AID_COUNT).toBeGreaterThanOrEqual(PAID_SAMPLE_COUNT);
  expect(WEBHOOK_SECRET.length, 'SANDBOX_WEBHOOK_SECRET is required').toBeGreaterThanOrEqual(16);

  const runId = `${Date.now()}-${Math.random().toString(16).slice(2, 8)}`;
  const seeds: AidSeed[] = [];
  const indexes = Array.from({ length: MEMBER_COUNT }, (_, i) => i);

  await inBatches(indexes, CONCURRENCY, async index => {
    const seed = await seedMember(index, runId);
    if (seed) seeds.push(seed);
  });

  expect(seeds).toHaveLength(AID_COUNT);

  const analyst = await operator('analyst@demo.local');
  const approver1 = await operator('approver1@demo.local');
  const approver2 = await operator('approver2@demo.local');
  const admin = await operator('admin@demo.local');
  const webhook = await request.newContext({ baseURL: BASE_URL });

  try {
    for (const seed of seeds.slice(0, PAID_SAMPLE_COUNT)) {
      const analysis = await jsonMutation(analyst, `/api/v1/aid-requests/${seed.id}/analysis`, {
        opinion: 'Documentação compatível na simulação controlada de piloto.'
      });
      expect(analysis.ok(), `analysis ${seed.id}: ${await analysis.text()}`).toBeTruthy();

      const fraud = await jsonMutation(analyst, `/api/v1/aid-requests/${seed.id}/fraud-screening`, {
        status: 'CLEARED', riskScore: 5, flags: 'Nenhum sinal relevante', note: 'Triagem sandbox concluída.'
      });
      expect(fraud.ok(), `fraud ${seed.id}: ${await fraud.text()}`).toBeTruthy();

      const firstApproval = await jsonMutation(approver1, `/api/v1/aid-requests/${seed.id}/approve`, {
        note: 'Primeira aprovação independente do piloto.'
      });
      expect(firstApproval.ok(), `approval1 ${seed.id}: ${await firstApproval.text()}`).toBeTruthy();

      const secondApproval = await jsonMutation(approver2, `/api/v1/aid-requests/${seed.id}/approve`, {
        note: 'Segunda aprovação independente do piloto.'
      });
      expect(secondApproval.ok(), `approval2 ${seed.id}: ${await secondApproval.text()}`).toBeTruthy();

      const idempotencyKey = `pilot-${runId}-${seed.id}`;
      const firstAttempt = await jsonMutation(admin, `/api/v1/payments/${seed.id}/initiate`, {}, { 'Idempotency-Key': idempotencyKey });
      expect(firstAttempt.ok(), `payment initiate ${seed.id}: ${await firstAttempt.text()}`).toBeTruthy();
      const attempt = await firstAttempt.json();
      expect(attempt.status).toBe('PROCESSING');

      if (seed === seeds[0]) {
        const retryAttempt = await jsonMutation(admin, `/api/v1/payments/${seed.id}/initiate`, {}, { 'Idempotency-Key': idempotencyKey });
        expect(retryAttempt.ok(), `idempotent retry ${seed.id}: ${await retryAttempt.text()}`).toBeTruthy();
        const retried = await retryAttempt.json();
        expect(retried.id).toBe(attempt.id);
        expect(retried.providerReference).toBe(attempt.providerReference);
      }

      const webhookBody = JSON.stringify({ providerReference: attempt.providerReference, status: 'SETTLED' });
      const timestamp = new Date().toISOString();
      const eventId = `pilot-event-${runId}-${seed.id}`;
      const signature = createHmac('sha256', WEBHOOK_SECRET).update(`${timestamp}.${webhookBody}`).digest('hex');
      const settled = await webhook.post('/api/v1/sandbox/webhooks/payment', {
        headers: {
          'X-Event-Id': eventId,
          'X-Timestamp': timestamp,
          'X-Signature': signature,
          'Content-Type': 'application/json'
        },
        data: { providerReference: attempt.providerReference, status: 'SETTLED' }
      });
      expect(settled.status(), `settlement ${seed.id}: ${await settled.text()}`).toBe(204);

      if (seed === seeds[0]) {
        const replay = await webhook.post('/api/v1/sandbox/webhooks/payment', {
          headers: {
            'X-Event-Id': eventId,
            'X-Timestamp': timestamp,
            'X-Signature': signature,
            'Content-Type': 'application/json'
          },
          data: { providerReference: attempt.providerReference, status: 'SETTLED' }
        });
        expect(replay.status()).not.toBe(204);
      }
    }

    const ledgerResponse = await admin.get('/api/v1/ledger');
    expect(ledgerResponse.ok()).toBeTruthy();
    const ledger = await ledgerResponse.json();

    const auditResponse = await admin.get('/api/v1/audit-events');
    expect(auditResponse.ok()).toBeTruthy();
    const audit = await auditResponse.json();

    for (const seed of seeds.slice(0, PAID_SAMPLE_COUNT)) {
      const attemptsResponse = await admin.get(`/api/v1/payments/${seed.id}`);
      expect(attemptsResponse.ok()).toBeTruthy();
      const attempts = await attemptsResponse.json();
      expect(attempts).toHaveLength(1);
      expect(attempts[0].status).toBe('SETTLED');

      const payments = ledger.filter((entry: { aidRequestId?: string; type: string }) =>
        entry.aidRequestId === seed.id && entry.type === 'AID_PAYMENT');
      expect(payments, `exactly one AID_PAYMENT for ${seed.id}`).toHaveLength(1);

      const events = audit.filter((event: { entityType: string; entityId: string }) =>
        event.entityType === 'AidRequest' && event.entityId === seed.id);
      expect(events.filter((event: { action: string }) => event.action === 'AID_APPROVED')).toHaveLength(2);
      expect(events.filter((event: { action: string }) => event.action === 'PAYMENT_SETTLED')).toHaveLength(1);
    }

    const transparencyResponse = await admin.get('/api/v1/transparency');
    expect(transparencyResponse.ok()).toBeTruthy();
    const transparency = await transparencyResponse.json();
    expect(transparency.activeMembers).toBeGreaterThanOrEqual(MEMBER_COUNT);
    expect(transparency.totalAidRequests).toBeGreaterThanOrEqual(AID_COUNT);
    expect(transparency.paidAidRequests).toBeGreaterThanOrEqual(PAID_SAMPLE_COUNT);

    console.log(JSON.stringify({
      phase: 8,
      scenario: 'pilot-simulation',
      runId,
      membersCreated: MEMBER_COUNT,
      aidRequestsCreated: AID_COUNT,
      paidSample: PAID_SAMPLE_COUNT,
      concurrency: CONCURRENCY,
      invariants: {
        distinctApprovals: true,
        exactlyOnceLedgerDebit: true,
        paymentIdempotency: true,
        webhookReplayProtection: true
      }
    }, null, 2));
  } finally {
    await Promise.all([analyst.dispose(), approver1.dispose(), approver2.dispose(), admin.dispose(), webhook.dispose()]);
  }
});
