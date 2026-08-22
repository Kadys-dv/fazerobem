import { test, expect, Page } from '@playwright/test';

test.describe.configure({ retries: 0 });

const DEMO_PASSWORD = 'Demo12345!';

async function login(page: Page, email: string) {
  await page.goto('/');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await page.locator('#loginEmail').fill(email);
  await page.locator('#loginPassword').fill(DEMO_PASSWORD);
  await page.locator('#loginForm').getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('button', { name: 'Sair' })).toBeVisible();
  const me = await page.request.get('/api/v1/auth/me');
  expect(me.ok(), `auth/me failed with ${me.status()}: ${await me.text()}`).toBeTruthy();
  const body = await me.json();
  expect(body.authenticated).toBe(true);
  expect(body.email).toBe(email);
}

async function logout(page: Page) {
  await page.goto('/');
  await page.getByRole('button', { name: 'Sair' }).click();
  await expect(page.getByRole('button', { name: 'Entrar' })).toBeVisible();
}

async function assertCaseVisibleToOperator(page: Page, aidId: string) {
  const response = await page.request.get('/api/v1/aid-requests');
  const text = await response.text();
  expect(response.ok(), `aid queue failed with ${response.status()}: ${text}`).toBeTruthy();
  const cases = JSON.parse(text);
  expect(cases.some((x: { id: string }) => x.id === aidId), `aid ${aidId} missing from operator queue: ${text}`).toBeTruthy();
}

async function openOperationalCase(page: Page, aidId: string, reason: string, role: string) {
  await assertCaseVisibleToOperator(page, aidId);
  await page.goto('/operations.html');
  await expect(page.getByRole('heading', { name: 'Fila operacional' })).toBeVisible();
  await expect(page.locator('#operatorIdentity')).toContainText(role, { timeout: 15_000 });
  await expect.poll(async () => Number(await page.locator('#caseCount').textContent()), { timeout: 15_000 }).toBeGreaterThan(0);
  const item = page.locator(`.case-item[data-id="${aidId}"]`);
  await expect(item).toBeVisible({ timeout: 15_000 });
  await expect(item).toContainText(reason);
  await item.click();
  await expect(page.locator('#caseReason')).toHaveText(reason);
}

test('full aid governance requires analyst screening and two distinct approvers with audit trail', async ({ page }) => {
  const reason = `Governança E2E ${Date.now()}`;

  await login(page, 'member@demo.local');
  await expect(page.locator('#memberRole')).toHaveText('MEMBER');

  await page.locator('#contributionAmount').fill('50,00');
  await page.locator('#contributionConsent').check();
  await page.getByRole('button', { name: 'Registrar contribuição sandbox' }).click();
  await expect(page.locator('#toast')).toContainText('Contribuição sandbox registrada');

  await page.locator('#aidCategory').selectOption('HEALTH');
  await page.locator('#aidAmount').fill('10,00');
  await page.locator('#aidReason').fill(reason);
  await page.getByRole('button', { name: 'Enviar pedido' }).click();
  await expect(page.locator('#toast')).toContainText('Pedido enviado para análise');
  await expect(page.locator('#aidDetailDialog')).toBeVisible();

  await page.locator('#documentType').selectOption('LAUDO_OU_RECEITA');
  await page.locator('#documentFile').setInputFiles({
    name: 'governanca-comprovante.png',
    mimeType: 'image/png',
    buffer: Buffer.from([0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a,0x00,0x00,0x00,0x00])
  });
  await page.getByRole('button', { name: 'Enviar documento' }).click();
  await expect(page.locator('#toast')).toContainText('Documento enviado com segurança');
  const aidId = await page.locator('#documentAidId').inputValue();
  await logout(page);

  await login(page, 'analyst@demo.local');
  await openOperationalCase(page, aidId, reason, 'ANALYST');
  await expect(page.locator('#analystActions')).toBeVisible();
  await expect(page.locator('#documentList')).toContainText('governanca-comprovante.png');

  await page.locator('#analysisOpinion').fill('Documentação compatível com a necessidade declarada.');
  await page.locator('#analysisForm').getByRole('button', { name: 'Registrar parecer' }).click();
  await expect(page.locator('#toast')).toContainText('Parecer registrado');
  await expect(page.locator('#historyList')).toContainText('Parecer do analista');

  await page.locator('#fraudStatus').selectOption('CLEARED');
  await page.locator('#riskScore').fill('10');
  await page.locator('#fraudFlags').fill('Nenhum sinal relevante');
  await page.locator('#fraudNote').fill('Triagem concluída sem indícios de fraude.');
  await page.locator('#fraudForm').getByRole('button', { name: 'Concluir triagem' }).click();
  await expect(page.locator('#toast')).toContainText('Triagem antifraude concluída');
  await expect(page.locator('#historyList')).toContainText('Antifraude: CLEARED');
  await expect(page.locator('#eligibilitySummary')).toContainText('Elegível pelos critérios atuais');
  await logout(page);

  await login(page, 'approver1@demo.local');
  await openOperationalCase(page, aidId, reason, 'APPROVER');
  await expect(page.locator('#approverActions')).toBeVisible();
  await page.locator('#approvalNote').fill('Primeira aprovação após análise documental e antifraude.');
  await page.locator('#approvalForm').getByRole('button', { name: 'Registrar aprovação' }).click();
  await expect(page.locator('#toast')).toContainText('Aprovação registrada');
  await expect(page.locator('#historyList')).toContainText('Aprovação 1');
  await expect(page.locator('#caseStatus')).toHaveText('Em análise');

  await page.locator('#approvalNote').fill('Tentativa duplicada do mesmo aprovador.');
  await page.locator('#approvalForm').getByRole('button', { name: 'Registrar aprovação' }).click();
  await expect(page.locator('#toast')).toContainText('Este usuário já aprovou o pedido');
  await logout(page);

  await login(page, 'approver2@demo.local');
  await openOperationalCase(page, aidId, reason, 'APPROVER');
  await page.locator('#approvalNote').fill('Segunda aprovação independente.');
  await page.locator('#approvalForm').getByRole('button', { name: 'Registrar aprovação' }).click();
  await expect(page.locator('#toast')).toContainText('Aprovação registrada');
  await expect(page.locator('#caseStatus')).toHaveText('Aprovado');
  await expect(page.locator('#historyList')).toContainText('Aprovação 2');
  await expect(page.locator('#historyList')).toContainText('Dupla aprovação concluída');
  await logout(page);

  await login(page, 'admin@demo.local');
  const detailResponse = await page.request.get(`/api/v1/operations/aid-requests/${aidId}`);
  expect(detailResponse.ok(), `case detail failed with ${detailResponse.status()}: ${await detailResponse.text()}`).toBeTruthy();
  const detail = await detailResponse.json();
  expect(detail.request.status).toBe('APPROVED');
  expect(detail.analyses).toHaveLength(1);
  expect(detail.fraudScreening.status).toBe('CLEARED');
  expect(detail.approvals).toHaveLength(2);
  expect(new Set(detail.approvals.map((x: { approverUserId: string }) => x.approverUserId)).size).toBe(2);

  const auditResponse = await page.request.get('/api/v1/audit-events');
  expect(auditResponse.ok(), `audit failed with ${auditResponse.status()}: ${await auditResponse.text()}`).toBeTruthy();
  const audit = await auditResponse.json();
  const events = audit.filter((x: { entityType: string; entityId: string }) => x.entityType === 'AidRequest' && x.entityId === aidId);
  expect(events.some((x: { action: string }) => x.action === 'AID_REQUEST_CREATED')).toBeTruthy();
  expect(events.some((x: { action: string }) => x.action === 'AID_DOCUMENT_REGISTERED')).toBeTruthy();
  expect(events.some((x: { action: string }) => x.action === 'AID_ANALYZED')).toBeTruthy();
  expect(events.some((x: { action: string }) => x.action === 'FRAUD_SCREENING_COMPLETED')).toBeTruthy();
  expect(events.filter((x: { action: string }) => x.action === 'AID_APPROVED')).toHaveLength(2);
});
