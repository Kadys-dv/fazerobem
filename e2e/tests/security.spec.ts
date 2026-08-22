import { test, expect, chromium } from '@playwright/test';

test('public transparency page responds', async ({ page }) => {
  const r = await page.goto('/');
  expect(r?.ok()).toBeTruthy();
});

test('WebAuthn virtual authenticator can be attached to Chromium', async () => {
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();
  const cdp = await context.newCDPSession(page);
  await cdp.send('WebAuthn.enable');
  const { authenticatorId } = await cdp.send('WebAuthn.addVirtualAuthenticator', { options: {
    protocol: 'ctap2', transport: 'internal', hasResidentKey: true,
    hasUserVerification: true, isUserVerified: true, automaticPresenceSimulation: true
  }});
  expect(authenticatorId).toBeTruthy();
  await cdp.send('WebAuthn.removeVirtualAuthenticator', { authenticatorId });
  await browser.close();
});
