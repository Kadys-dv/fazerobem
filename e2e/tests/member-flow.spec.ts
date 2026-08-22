import { test, expect } from '@playwright/test';

test('member can register, contribute in sandbox and request aid', async ({ page }) => {
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

  await page.locator('#contributionAmount').fill('25,00');
  await page.locator('#contributionConsent').check();
  await page.getByRole('button', { name: 'Registrar contribuição sandbox' }).click();
  await expect(page.locator('#toast')).toContainText('Contribuição sandbox registrada');

  const category = page.locator('#aidCategory');
  await category.selectOption({ index: 1 });
  await page.locator('#aidAmount').fill('10,00');
  await page.locator('#aidReason').fill('Pedido automatizado de validação E2E do fluxo de ajuda mútua.');
  await page.getByRole('button', { name: 'Enviar pedido' }).click();

  await expect(page.locator('#toast')).toContainText('Pedido enviado para análise');
  await expect(page.locator('#myRequests')).toContainText('R$ 10,00');
  await expect(page.locator('#myRequests')).toContainText('Em análise');
});
