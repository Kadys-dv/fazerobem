import { test, expect, Page } from '@playwright/test';

const PASSWORD='Demo12345!';

async function login(page:Page,email:string){
  await page.goto('/');
  await page.getByRole('button',{name:'Entrar'}).click();
  await page.locator('#loginEmail').fill(email);
  await page.locator('#loginPassword').fill(PASSWORD);
  await page.locator('#loginForm').getByRole('button',{name:'Entrar'}).click();
  await expect(page.getByRole('button',{name:'Sair'})).toBeVisible();
  await page.goto('/operations.html');
  await expect(page.getByRole('heading',{name:'Fila operacional'})).toBeVisible();
}

async function logout(page:Page){
  await page.goto('/');
  await page.getByRole('button',{name:'Sair'}).click();
}

test('operational panel preserves separation of duties and exposes auditor trail',async({page})=>{
  await login(page,'analyst@demo.local');
  await expect(page.locator('#operatorIdentity')).toContainText('ANALYST');
  await expect(page.locator('#analystActions')).toBeHidden();
  await expect(page.locator('#paymentActions')).toBeHidden();
  await expect(page.getByText(/forçar\s+paid/i)).toHaveCount(0);
  await logout(page);

  await login(page,'approver1@demo.local');
  await expect(page.locator('#operatorIdentity')).toContainText('APPROVER');
  await expect(page.locator('#paymentActions')).toBeHidden();
  await expect(page.getByText(/forçar\s+paid/i)).toHaveCount(0);
  await logout(page);

  await login(page,'admin@demo.local');
  await expect(page.locator('#operatorIdentity')).toContainText('ADMIN');
  await expect(page.locator('#analystActions')).toBeHidden();
  await expect(page.locator('#approverActions')).toBeHidden();
  await expect(page.getByText(/forçar\s+paid/i)).toHaveCount(0);
  await logout(page);

  await login(page,'auditor@demo.local');
  await expect(page.locator('#operatorIdentity')).toContainText('AUDITOR');
  await expect(page.locator('#analystActions')).toBeHidden();
  await expect(page.locator('#approverActions')).toBeHidden();
  await expect(page.locator('#paymentForm')).toBeHidden();
  await expect(page.locator('#reconciliationForm')).toBeHidden();
  await expect(page.locator('#auditorTrail')).toBeVisible({timeout:10_000});
  await expect(page.locator('#auditList')).toBeVisible();
  await expect(page.getByText(/forçar\s+paid/i)).toHaveCount(0);
});
