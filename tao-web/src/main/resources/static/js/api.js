/**
 * 前端统一请求工具
 * - 自动携带 token
 * - 统一处理 401（未登录）
 * - 统一消息提示
 */
const API = {
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),

    /** 基础请求 */
    async request(url, options = {}) {
        const headers = { 'Content-Type': 'application/json' };
        if (this.token) {
            headers['Authorization'] = this.token;
        }
        try {
            const resp = await fetch(url, {
                ...options,
                headers: Object.assign(headers, options.headers || {})
            });
            const data = await resp.json();
            // token 失效或被拦截
            if (data.code === 401) {
                this.clearLogin();
                if (!location.pathname.endsWith('login.html')) {
                    location.href = 'login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
                }
            }
            return data;
        } catch (e) {
            this.toast('网络异常，请检查服务是否已启动');
            return { code: 500, msg: '网络异常' };
        }
    },

    get(url) {
        return this.request(url);
    },

    post(url, body) {
        return this.request(url, { method: 'POST', body: JSON.stringify(body || {}) });
    },

    put(url, body) {
        return this.request(url, { method: 'PUT', body: JSON.stringify(body || {}) });
    },

    del(url, body) {
        return this.request(url, { method: 'DELETE', body: body ? JSON.stringify(body) : undefined });
    },

    /** 保存登录信息 */
    saveLogin(res) {
        if (res.token) {
            this.token = res.token;
            localStorage.setItem('token', res.token);
        }
        if (res.userInfo) {
            this.user = res.userInfo;
            localStorage.setItem('user', JSON.stringify(res.userInfo));
        }
    },

    clearLogin() {
        this.token = '';
        this.user = null;
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    isLogin() {
        return !!this.token;
    },

    /** 需要登录才能访问的页面调用 */
    requireLogin() {
        if (!this.isLogin()) {
            location.href = 'login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
            return false;
        }
        return true;
    },

    /** 轻提示 */
    toast(msg) {
        const el = document.createElement('div');
        el.className = 'toast';
        el.textContent = msg;
        document.body.appendChild(el);
        setTimeout(() => el.remove(), 2200);
    },

    /** 渲染顶部导航条 */
    renderHeader(active) {
        const nav = document.getElementById('header');
        if (!nav) return;
        const links = [
            { key: 'index', text: '首页', href: 'index.html' },
            { key: 'publish', text: '发布商品', href: 'publish.html' },
            { key: 'my', text: '我的', href: 'my.html' },
            { key: 'profile', text: '个人资料', href: 'profile.html' }
        ];
        const right = this.isLogin()
            ? `<span class="user-name">你好，${this.user ? (this.user.nickName || this.user.userName) : ''}</span>
               <a href="javascript:void(0)" onclick="logout()">退出登录</a>`
            : `<a href="login.html">登录</a><a href="register.html">注册</a>`;

        nav.innerHTML = `
            <div class="nav">
                <a href="index.html" class="logo">校园淘</a>
                ${links.map(l => `<a href="${l.href}" class="${active === l.key ? 'active' : ''}">${l.text}</a>`).join('')}
                <div class="nav-right">${right}</div>
            </div>`;
    },

    /** 取当前页码参数 */
    param(name, def) {
        const v = new URLSearchParams(location.search).get(name);
        return v ? decodeURIComponent(v) : def;
    },

    /** 格式化金额 */
    money(v) {
        return v == null ? '0.00' : Number(v).toFixed(2);
    },

    /** 格式化时间 */
    time(v) {
        return v ? String(v).substring(0, 16) : '';
    },

    /** 商品封面（无图时显示占位文字） */
    coverHtml(url, text) {
        return url ? `<img src="${url}" alt="${text || ''}">` : (text || '暂无图片');
    },

    /**
     * 下载已上传的文件（走 /web/common/download，需登录）
     * 下载接口需要登录，而 <a> 直链无法带自定义请求头，
     * 因此把 token 通过 query 参数带上（拦截器已兼容从 query 取 token），
     * 并在用户点击的手势内同步触发，避免 fetch 异步导致的“静默不下载”
     * @param {string} url 上传返回的完整 URL，如 /uploads/xxx.jpg
     * @param {string} [name] 下载保存的文件名
     */
    downloadFile(url, name) {
        const fileName = (url || '').replace('/uploads/', '');
        if (!fileName) {
            this.toast('文件地址无效');
            return;
        }
        const a = document.createElement('a');
        a.href = '/web/common/download?fileName=' + encodeURIComponent(fileName)
            + '&token=' + encodeURIComponent(this.token);
        if (name) a.download = name;
        document.body.appendChild(a);
        a.click();
        a.remove();
    }
};

/** 退出登录 */
function logout() {
    API.post('/web/user/logout').then(() => {
        API.clearLogin();
        location.href = 'index.html';
    });
}
