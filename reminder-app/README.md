# ⏰ 提醒助手 Android App

每20分钟推送提醒，25秒后再次提醒，支持定时自动开启/关闭。

## 功能说明

- **主开关**：开启后每20分钟推送一次，25秒后再推送一次，无限循环
- **定时自动开启**：每天到达设定时间自动开启提醒
- **定时自动关闭**：每天到达设定时间自动关闭提醒
- **开机自启**：若之前是开启状态，手机重启后自动恢复

---

## 🚀 通过 GitHub Actions 编译 APK（推荐）

### 第一步：上传代码到 GitHub

1. 登录 [github.com](https://github.com)，点击右上角 **+** → **New repository**
2. 仓库名填 `reminder-app`，设为 **Public**，点击 **Create repository**
3. 把本项目所有文件上传到仓库

   **方法A（网页上传）**：
   - 点击 "uploading an existing file"
   - 把整个项目文件夹拖入网页
   - 点击 "Commit changes"

   **方法B（命令行）**：
   ```bash
   cd reminder-app
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/你的用户名/reminder-app.git
   git push -u origin main
   ```

### 第二步：触发编译

代码推送到 main 分支后，GitHub Actions 会**自动开始编译**。

你也可以手动触发：
1. 打开仓库页面 → 点击 **Actions** 标签
2. 左侧点击 **Build Reminder App APK**
3. 点击 **Run workflow** → **Run workflow**

### 第三步：下载 APK

1. 编译完成后（约3-5分钟），进入 **Actions** 页面
2. 点击最新的工作流运行记录
3. 页面底部 **Artifacts** 区域，点击 **reminder-app-debug** 下载
4. 解压 zip 文件，得到 `app-debug.apk`

### 第四步：安装到手机

1. 手机打开 **设置 → 安全 → 允许安装未知来源应用**
2. 用数据线传输 APK 到手机，或通过微信/QQ发送
3. 点击 APK 文件安装
4. 首次打开时允许**通知权限**

---

## ⚠️ 重要说明

- **Android 12+**：系统可能限制精确闹钟，若定时开关不生效，请到 `设置 → 应用 → 提醒助手 → 闹钟和提醒` 开启权限
- **省电模式**：部分手机（小米/华为/OPPO等）会杀死后台服务，请将本应用加入**电池白名单**或**受保护应用**
- **通知权限**：Android 13+ 需要手动允许通知权限

---

## 项目结构

```
reminder-app/
├── .github/workflows/build-apk.yml   # GitHub Actions 配置
├── app/
│   ├── build.gradle
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/reminder/app/
│   │   │   ├── MainActivity.kt       # 主界面
│   │   │   ├── ReminderService.kt    # 后台提醒服务
│   │   │   ├── AlarmReceiver.kt      # 定时开关接收器
│   │   │   └── BootReceiver.kt       # 开机自启接收器
│   │   └── res/
│   │       ├── layout/activity_main.xml
│   │       └── values/
├── build.gradle
├── settings.gradle
└── gradlew
```
