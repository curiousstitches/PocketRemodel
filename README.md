# 🛋️ Pocket House Remodeling

**Talk to your real room. Watch it transform. Walk around the result.**

Point your phone at your living room, hold the mic, and say *"remove that pile of
boxes and put a walnut coffee table there."* The clutter vanishes, a real 3D table
drops onto your floor, and you can crouch down to look underneath it. Hit **Save**,
come back tomorrow, scan for 3 seconds — your remodel is still there.

This is a **complete, real Android app project**, built exactly to the blueprint:
ARCore + SceneView (Filament) for the AR, your OpenRouter key for the AI brain,
on-device voice in/out, and Cloud Anchors for the "time machine" save feature.

---

## ⚡ The 4 things you do once (no coding required)

You don't write a single line. You just plug in 4 things, press Run, and it's live.

### 1. Install Android Studio (free)
Download **Android Studio** from https://developer.android.com/studio and install it.
Open it once so it finishes setting itself up.

### 2. Open this project
In Android Studio: **File → Open →** select the `PocketRemodel` folder.
It will think for a minute (downloading the engine pieces) the first time. Let it finish.

### 3. The AI key — the app does this FOR you now
You don't touch any settings file. The **first time the app opens**, it shows a
setup screen that:
1. Has a **"Get my free key"** button → it opens OpenRouter for you (the app stays
   open in the background — just press back when you've copied your key).
2. Lets you **paste** the key (with a one-tap "Paste from clipboard" button).
3. Has a **"Test connection"** button that turns **green ✅** if it works or
   **red ❌** if it doesn't — so you know instantly.

You do this once; the app remembers it forever. That single key gives access to
*every* top AI model and auto-switches between **free** ones if one is busy —
that's the "9Router" idea, built right in.

> *Advanced/optional:* you can pre-load a key by renaming `local.properties.example`
> to `local.properties` and pasting it there — but you don't need to.

### 4. Drop in your furniture (the 3D models)
Open **`app/src/main/assets/models/`** and read the short `README.txt` there.
Drop in free `.glb` furniture files (links to free CC0 sources are in that file).
Filenames are listed for you — match them and the AI instantly knows about each item.

> The app won't crash if a model is missing — that item just won't appear. So you can
> start with even one or two `.glb` files and add more anytime.

### ▶️ Press Run
Plug in an **ARCore-supported Android phone** (most phones from the last ~5 years —
full list: https://developers.google.com/ar/devices), press the green **▶ Run**
button in Android Studio, and the app installs and launches on your phone. Done.

---

## 📤 "Do I have to plug in my computer every time?" — NO

You build the app **once**. After that you have a real, standalone Android app you
can install and share like any other — no computer, no cable, ever again.

**To get the shareable file:** in Android Studio, top menu →
**Build → Build Bundle(s) / APK(s) → Build APK(s)**. When it finishes, click the
**"locate"** popup. That gives you a single file: **`app-debug.apk`**.

- **Put it on your own phone:** copy the APK over, tap it, install. Runs by itself.
- **Send it to a friend:** text/email/upload the APK; they tap it to install
  (they just toggle "allow install from this source" once — Android asks
  automatically). The app then runs 100% on *their* phone.
- **Want a public install link / app store?** Upload the APK (or a signed "release
  bundle") to the **Google Play Console** ($25 one-time) or a free host like
  **Firebase App Distribution** / **Diawi**, and you get a link anyone can tap.

Each person who installs it does the one-time key setup on first open — that's it.

> The computer is only ever the workshop. The APK is the finished product, and it's
> fully independent.

---

## 🧠 How it works under the hood (the simple version)

```
You hold the mic and speak
        ↓
Phone turns your voice into text   (on-device, free)
        ↓
Text + a strict "interior-design-only" rulebook → OpenRouter
        ↓
AI replies with a tidy instruction list (add table / hide boxes)
        ↓
The AR engine places real 3D furniture & hides clutter on your floor
        ↓
The assistant reads its reply back to you out loud
```

Everything runs on the **one phone**. No separate computer or server is needed once
it's installed.

---

## ✨ What's built and working

| Feature | Status | Where |
|---|---|---|
| Fancy animated splash screen | ✅ | `ui/SplashScreen.kt` |
| In-app key setup with green/red test | ✅ | `ui/SetupScreen.kt` |
| Easy furniture drawer (search + tap to place) | ✅ | `ui/FurnitureSheet.kt` |
| Add furniture from a PHOTO (AI vision match) | ✅ | `ui/FurnitureSheet.kt` + `ai/OpenRouterClient.kt` |
| Live AR camera + plane/floor detection | ✅ | `ui/ArRemodelScreen.kt` |
| Hold-to-talk voice in + spoken replies | ✅ | `voice/VoiceManager.kt` |
| AI brain with free-model auto-failover ("9Router") | ✅ | `ai/OpenRouterClient.kt` |
| Design-only guardrails (stays on topic) | ✅ | `ai/SystemPrompt.kt` |
| Voice → typed commands translator | ✅ | `ai/CommandParser.kt` |
| Place / remove / recolor / move 3D furniture | ✅ | `ar/SceneController.kt` |
| Look-under real 3D models | ✅ (built into .glb) | `ar/SceneController.kt` |
| Diminishing reality (hide real clutter) | ✅ v1 (floor patch) | `ar/DiminishedRealityRenderer.kt` |
| Save design locally (the "time machine") | ✅ | `ar/PersistenceManager.kt` |
| Cloud Anchor room-map save/restore | ✅ | `ar/CloudAnchorService.kt` |
| Furniture catalog (easy to extend) | ✅ | `data/ModelCatalog.kt` |

---

## 🔓 One optional extra: the "recognize this room days later" feature

The basic save (what you placed + where) works out of the box. The deeper magic —
the phone *recognizing the same physical room* days later — uses **ARCore Cloud
Anchors**, which needs a free Google switch flipped once:

1. Go to https://console.cloud.google.com → create a project (free).
2. Search **"ARCore API"** → **Enable**.
3. Create an **API key** and add it to your `local.properties` as `ARCORE_API_KEY=...`
   *(wire-up notes are in `CloudAnchorService.kt`)*.

The free tier is plenty for launch and personal use. Skip this and the app still
saves and restores designs locally — you just rescan from scratch each session.

---

## 💯 Honest notes (so there are zero surprises)

- **It must run on a real phone.** ARCore needs a physical camera + motion sensors,
  so the emulator won't do AR. This is true of every AR app, not just this one.
- **"Diminishing reality" v1 is a smart cover-up, not sci-fi deletion.** It lays a
  floor-textured patch over real clutter so it disappears as you move. The exact
  spot to upgrade it to live neural inpainting is marked in the code
  (`DiminishedRealityRenderer.kt`) — drop-in ready for a future version.
- **Free AI models have daily limits.** If you hit them, add a few dollars of credit
  on OpenRouter or the app auto-falls-back down its model chain.
- **SceneView version:** built against `4.17.0` (current). If Google ships a newer
  one with a tweaked function name, Android Studio underlines it in red and suggests
  the fix in one click.

---

## 📁 Project map

```
PocketRemodel/
├─ local.properties.example   ← rename to local.properties, paste your key
├─ README.md                  ← you are here
└─ app/src/main/
   ├─ assets/models/          ← drop your .glb furniture here
   └─ java/com/pocketremodel/app/
      ├─ MainActivity.kt          ← asks for camera/mic, launches the screen
      ├─ ArViewModel.kt           ← the conductor (voice→AI→scene loop)
      ├─ ui/ArRemodelScreen.kt    ← the live AR screen + controls
      ├─ ai/                      ← OpenRouter brain, guardrails, parser
      ├─ voice/                   ← speech-to-text + text-to-speech
      ├─ ar/                      ← placing furniture, hiding clutter, saving
      ├─ data/ModelCatalog.kt     ← the furniture library
      └─ domain/                  ← the command vocabulary
```

Built for launch. Plug in the 4 things, press Run, redesign your world. 🚀
