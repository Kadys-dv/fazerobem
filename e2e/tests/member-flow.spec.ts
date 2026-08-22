import { test, expect } from '@playwright/test';

test('member onboarding is enforced server-side before sandbox contribution and aid', async ({ page }) => {
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

  const bypass = await page.evaluate(async () => {
    const me = await fetch('/api/v1/auth/me', { credentials: 'same-origin' }).then(r => r.json());
    const csrf = await fetch('/api/v1/auth/csrf', { credentials: 'same-origin' }).then(r => r.json());
    const response = await fetch('/api/v1/contributions', {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },
      body: JSON.stringify({ memberId: me.memberId, amount: '5.00' }),
    });
    return { status: response.status, body: await response.json() };
  });
  expect(bypass.status).toBe(400);
  expect(String(bypass.body.error)).toContain('Primeiro acesso pendente');

  await page.locator('#acceptTerms').check();
  await page.locator('#acceptPrivacy').check();
  await page.locator('#acceptRules').check();
  await page.getByRole('button', { name: 'Aceitar e continuar' }).click();
  await expect(page.locator('#onboardingDialog')).not.toBeVisible();

  await page.locator('#contributionAmount').fill('25,00');
  await page.locator('#contributionConsent').check();
  await page.getByRole('button', { name: 'Registrar contribuição sandbox' }).click();
  await expect(page.locator('#toast')).toContainText('Contribuição sandbox registrada');
  await expect(page.locator('#myContributionTotal')).toContainText('25,00');
  await expect(page.locator('#myContributions')).toContainText('Contribuição voluntária');

  const category = page.locator('#aidCategory');
  await category.selectOption({ index: 1 });
  await page.locator('#aidAmount').fill('10,00');
  await page.locator('#aidReason').fill('Pedido automatizado de validação E2E do fluxo de ajuda mútua.');
  await page.getByRole('button', { name: 'Enviar pedido' }).click();

  await expect(page.locator('#toast')).toContainText('Pedido enviado para análise');
  await expect(page.locator('#myRequests')).toContainText('R$ 10,00');
  await expect(page.locator('#myRequests')).toContainText('Em análise');
  await expect(page.locator('#requestDialog')).toBeVisible();
  await expect(page.locator('#eligibilityBox')).toContainText('Critérios de elegibilidade');
});
