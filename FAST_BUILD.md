# ⚡ Fast Build — no Android Studio, no big downloads

Your internet is slow and Android Studio is **gigabytes**. So skip it entirely.
Let Google's free servers build the app in the cloud. The only thing you download
is the finished app file at the very end (~15–30 MB). Total typing: almost none.

---

## Do this once

### 1. Make a free GitHub account
Go to **github.com** → Sign up. (Free. No card.)

### 2. Make a new empty project ("repository")
- Click the **+** (top-right) → **New repository**.
- Name it `PocketRemodel` → keep it **Public** or **Private** (either is fine) →
  click **Create repository**.

### 3. Upload the project folder
- On the new repo page, click **“uploading an existing file”** (a link in the
  middle of the page).
- Drag the **entire contents** of the `PocketRemodel` folder into the box
  (or click “choose your files” and select them).
- Click **Commit changes** at the bottom.

> 📤 This upload is small (just text files — a few MB total), so even slow internet
> handles it fine.

### 4. Let it build itself
- The moment you upload, the build starts automatically.
- Click the **Actions** tab (top of the repo). You'll see a job running with a
  spinning yellow dot. Wait ~3–6 minutes for a **green check ✅**.
  - *(If it doesn't auto-start, click the green “Build APK” workflow → “Run workflow”.)*

### 5. Download the finished app to your phone
- Click the finished (green ✅) build.
- Scroll to the bottom to the **Artifacts** box → tap **`PocketRemodel-app`**.
- It downloads a small zip. Open it → inside is **`app-debug.apk`** — that's your app.

### 6. Install it
- On your phone, tap the `app-debug.apk` file.
- Android asks permission to “install from this source” the first time → tap
  **Allow / Settings → enable**, then tap **Install**.
- Open **Pocket Remodel**. The setup screen walks you through your free AI key.
  Done. 🎉

---

## Why this is the fast path

| | Android Studio way | Cloud build (this) |
|---|---|---|
| Big download on your slow internet | ❌ Several GB | ✅ None |
| Software to install on PC | ❌ Yes | ✅ None |
| USB cable | ❌ Needed | ✅ Not needed |
| What you download at the end | the APK | the APK (~15–30 MB) |

After the first time, any change you upload rebuilds automatically — just grab the
new APK from the **Actions** tab.

---

## Sharing with someone else (still no rebuild for them)
Send them the `app-debug.apk` file (text, email, cloud drive, or a link). They tap
it, allow install once, and it runs entirely on their phone. Each person enters
their own free key on first open.
