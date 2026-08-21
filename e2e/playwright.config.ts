import { defineConfig } from '@playwright/test';
export default defineConfig({testDir:'./tests',use:{baseURL:process.env.BASE_URL||'http://localhost:8080',trace:'retain-on-failure'},retries:1,reporter:'html'});
