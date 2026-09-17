import { expect, test } from '@playwright/test';

/**
 * Covers the login → role-based landing route flow end to end, through a real browser against
 * the real backend (see e2e/README.md for prerequisites). It intentionally doesn't try to reach
 * the member `/dashboard` itself — a fresh member has no assessment yet, so the app correctly
 * sends them to `/assessment` first; completing the multi-step assessment form is its own,
 * separate journey.
 */

const COACH_EMAIL = 'coach@powerfitness.com';
const COACH_PASSWORD = 'PowerCoach123!';

function uniqueEmail(prefix: string): string {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 10_000)}@example.com`;
}

test.describe('login', () => {
  test('a coach signs in and lands on the coach dashboard', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Email Address').fill(COACH_EMAIL);
    await page.getByLabel('Password', { exact: true }).fill(COACH_PASSWORD);
    await page.getByRole('button', { name: /sign in to dashboard/i }).click();

    await expect(page).toHaveURL(/\/coach(\/|$)/);
    await expect(page.getByRole('heading', { name: /coach\s*dashboard/i })).toBeVisible({ timeout: 15_000 });
  });

  test('a brand-new member registers, is signed in, and lands on the assessment', async ({ page }) => {
    const email = uniqueEmail('e2e-member');

    await page.goto('/register');
    await page.getByLabel('Full Name').fill('E2E Test Member');
    await page.getByLabel('Email Address').fill(email);
    await page.getByLabel('Password', { exact: true }).fill('SuperSecret1');
    await page.getByLabel('Confirm Password').fill('SuperSecret1');
    await page.getByRole('button', { name: /create account/i }).click();

    await expect(page).toHaveURL(/\/assessment/);
    await expect(page.getByText(/step 1 of/i)).toBeVisible();
  });

  test('wrong credentials show an inline error and keep the user on the login page', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Email Address').fill(uniqueEmail('nobody'));
    await page.getByLabel('Password', { exact: true }).fill('WhateverPassword1');
    await page.getByRole('button', { name: /sign in to dashboard/i }).click();

    await expect(page.locator('.alert-error')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('an unauthenticated visitor hitting a member page is redirected to login with a returnUrl', async ({ page }) => {
    await page.goto('/dashboard');

    await expect(page).toHaveURL(/\/login\?returnUrl=%2Fdashboard/);
  });
});
