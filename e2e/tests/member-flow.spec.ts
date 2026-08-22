import { test, expect } from '@playwright/test';

test('member can onboard, contribute, request aid and upload a protected document', async ({ page }) => {
  const suffix = `${Date.now()}-${Math.random().toString(16).slice(2)}`;
  const email = `member-${suffix}@example.test`;

  await page.goto('/');
  await expect(page.getByRole('heading', { name: /Uma comunidade que transforma/i })).toBeVisible();
  await expect(page.locator('#balance')).not.toHaveText('—');

  await page.getByRole('button', { name: 'Participar da comunidade' }).click();
  const registerForm = page.locator('#registerForm');
  await registerForm.locator('#registerName').fill('Membro E2E');
  await registerForm.locator('#registerEmail').fill(email);
  await registerForm.locator('#registerPassword').fill('TestOnly123!');
  await registerForm.getByRole('button', { name: 'Criar conta' }).click();

  await expect(page.locator('#memberArea')).toBeVisible();
  await expect(page.locator('#memberRole')).toHaveText('MEMBER');
  await expect(page.locator('#onboardingDialog')).toBeVisible();

  await page.locator('#acceptTerms').check();
  await page.locator('#acceptPrivacy').check();
  await page.locator('#acceptRules').check();
  await page.getByRole('button', { name: 'Concluir primeiro acesso' }).click();
  await expect(page.locator('#toast')).toContainText('Primeiro acesso concluído');
  await expect(page.locator('#onboardingDialog')).not.toBeVisible();

  await page.locator('#contributionAmount').fill('25,00');
  await page.locator('#contributionConsent').check();
  await page.getByRole('button', { name: 'Registrar contribuição sandbox' }).click();
  await expect(page.locator('#toast')).toContainText('Contribuição sandbox registrada');
  await expect(page.locator('#myContributions')).toContainText('R$ 25,00');

  await page.locator('#aidCategory').selectOption('HEALTH');
  await page.locator('#aidAmount').fill('10,00');
  await page.locator('#aidReason').fill('Pedido automatizado de validação E2E do fluxo de ajuda mútua.');
  await page.getByRole('button', { name: 'Enviar pedido' }).click();

  await expect(page.locator('#toast')).toContainText('Pedido enviado para análise');
  await expect(page.locator('#myRequests')).toContainText('R$ 10,00');
  await expect(page.locator('#myRequests')).toContainText('Em análise');
  await expect(page.locator('#aidDetailDialog')).toBeVisible();
  await expect(page.locator('#eligibilityBox')).toContainText('Documento comprobatório obrigatório');

  await page.locator('#documentType').selectOption('LAUDO_OU_RECEITA');
  await page.locator('#documentFile').setInputFiles({
    name: 'comprovante.png',
    mimeType: 'image/png',
    buffer: Buffer.from([0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a,0x00,0x00,0x00,0x00])
  });
  await page.getByRole('button', { name: 'Enviar documento' }).click();

  await expect(page.locator('#toast')).toContainText('Documento enviado com segurança');
  await expect(page.locator('#documentList')).toContainText('comprovante.png');
  await expect(page.locator('#eligibilityBox')).toContainText('Documentos enviados: 1');

  const aidId = await page.locator('#documentAidId').inputValue();
  const response = await page.request.get(`/api/v1/aid-requests/${aidId}/documents`);
  expect(response.ok()).toBeTruthy();
  const documents = await response.json();
  expect(documents).toHaveLength(1);
  expect(documents[0].fileName).toBe('comprovante.png');
  expect(documents[0].storageKey).toBeUndefined();
  expect(documents[0].sha256).toBeUndefined();
  expect(documents[0].submittedByUserId).toBeUndefined();
});
