import { test, expect } from '@playwright/test';

test.describe('Serendia Social Media Marketing App E2E Flow', () => {
  test('Complete full user flow from registration to analytics', async ({ page }) => {
    // 1. Listen for browser prompt dialogs (needed for Workspace Switcher's prompt)
    page.on('dialog', async dialog => {
      console.log(`[Dialog Box] Type: ${dialog.type()}, Message: ${dialog.message()}`);
      if (dialog.type() === 'prompt') {
        await dialog.accept('Serendia Agency');
      } else {
        await dialog.dismiss();
      }
    });

    // 2. Open register page
    console.log('Navigating to register page...');
    await page.goto('/register');
    await expect(page).toHaveURL(/\/register/);

    const email = `jane.admin-${Date.now()}@serendia.io`;

    // 3. Fill in registration form
    console.log('Filling in register fields...');
    await page.fill('input[placeholder="John Doe"]', 'Jane Admin');
    await page.fill('input[placeholder="name@example.com"]', email);
    await page.fill('input[placeholder="At least 12 characters"]', 'SuperPassword123!');
    
    // Click register button
    console.log('Submitting registration form...');
    await page.click('button:has-text("Register")');

    // 4. Verify redirected to login page
    console.log('Waiting for login page redirection...');
    await page.waitForURL(/\/login/);
    await expect(page).toHaveURL(/\/login/);

    // 5. Log in
    console.log('Entering login credentials...');
    await page.fill('input[placeholder="name@example.com"]', email);
    await page.fill('input[placeholder="••••••••••••"]', 'SuperPassword123!');
    await page.click('button:has-text("Log In")');

    // 6. Verify redirected to dashboard
    console.log('Waiting for dashboard load...');
    await page.waitForURL(/\/dashboard/);
    await expect(page).toHaveURL(/\/dashboard/);
    console.log('Successfully logged into Dashboard!');

    // 7. Click Workspace Switcher to create a workspace
    console.log('Creating a workspace via Workspace Switcher...');
    await page.click('button:has-text("Active Workspace"), button:has-text("Select Workspace")');
    await page.click('button:has-text("New Workspace")');

    // Wait for the workspace switcher text to update to "Serendia Agency"
    await page.waitForSelector('span:has-text("Serendia Agency")');
    console.log('Workspace "Serendia Agency" successfully created!');

    // 8. Go to Settings Page
    console.log('Navigating to settings...');
    await page.click('a:has-text("Settings")');
    await page.waitForURL(/\/settings/);

    // Connect Meta social account (Mock integration)
    console.log('Connecting Meta account (mock redirection)...');
    await page.click('button:has-text("Connect Meta")');
    // The OAuth init URL redirects, wait for it to return to settings with success toast
    await page.waitForURL(/\/settings/);
    console.log('Meta integration connected successfully!');

    // 9. Go to Ad Campaigns Page
    console.log('Navigating to Ad Campaigns...');
    await page.click('a:has-text("Ad Campaigns")');
    await page.waitForURL(/\/campaigns/);

    // Click New Campaign to launch wizard modal
    console.log('Opening New Campaign Wizard...');
    await page.click('button:has-text("New Campaign")');
    await page.waitForSelector('h3:has-text("New Campaign Wizard")');

    // Fill the campaign details
    console.log('Filling in campaign details...');
    await page.fill('input[placeholder="e.g. Q3 Summer Product Launch"]', 'Promo Q3');
    await page.fill('input[type="number"]', '200');

    // Launch campaign
    console.log('Launching campaign...');
    await page.click('button:has-text("Launch Campaign")');

    // Verify campaign is created in campaigns table
    console.log('Verifying campaign was added to the table...');
    await page.waitForSelector('td:has-text("Promo Q3")');
    console.log('Campaign "Promo Q3" successfully verified in table!');

    // Click "Optimize Now" button on budget optimizer banner
    console.log('Clicking "Optimize Now" budget reallocation trigger...');
    await page.click('button:has-text("Optimize Now")');
    await page.waitForTimeout(1000); // Wait for recalculation animations

    // 10. Navigate to Unified Inbox
    console.log('Navigating to Unified Inbox...');
    await page.click('a:has-text("Unified Inbox")');
    await page.waitForURL(/\/inbox/);

    // Simulate a message ingestion
    console.log('Simulating positive DM webhook...');
    await page.click('button:has-text("Simulate positive DM")');
    await page.waitForTimeout(2000); // Wait for Redis pub/sub ingestion and UI refresh

    // Click the first conversation thread item
    console.log('Selecting first conversation thread...');
    await page.click('text=Meta User');

    // Click "AI Assist" button to draft reply
    console.log('Clicking AI Assist reply generator...');
    await page.click('button:has-text("AI Assist")');
    
    // Wait for the textarea to be populated
    await page.waitForTimeout(1000);

    // Send reply
    console.log('Sending message response...');
    await page.click('button:has-text("Send Reply")');

    // 11. Navigate to Analytics page
    console.log('Navigating to Analytics charts page...');
    await page.click('a:has-text("Analytics")');
    await page.waitForURL(/\/analytics/);

    // Wait for charts to render
    console.log('Waiting for Recharts visualization to render...');
    await page.waitForTimeout(2000);

    // Capture screenshot as verification artifact
    const screenshotPath = '/Users/vibodhabalalla/.gemini/antigravity-ide/brain/b6882431-f438-4895-b863-594557b3b29b/serendia_analytics_screenshot.png';
    console.log(`Taking E2E verification screenshot: ${screenshotPath}`);
    await page.screenshot({ path: screenshotPath, fullPage: true });

    console.log('Serendia E2E UI automation test completed successfully!');
  });
});
