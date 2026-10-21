/**
 * 校园淘 智能客服浮窗组件（原生 JS，自包含，无第三方依赖）
 * 用法：在页面 </body> 前引入 <script src="js/customer-service.js"></script>
 * 依赖：同域的 /api/consultStream（SSE 流式）与 /web/common/upload（图片上传）
 *      可选依赖：全局 API 对象（用于取 token / 当前用户 id）
 */
(function () {
  'use strict';

  var BRAND = '#ff6b35';

  /* ---------------- 安全 Markdown 渲染（先转义再格式化，防 XSS） ---------------- */
  function escapeHtml(s) {
    return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }
  function inlineMd(s) {
    s = s.replace(/`([^`]+)`/g, function (_, c) { return '<code>' + c + '</code>'; });
    s = s.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    s = s.replace(/\*([^*]+)\*/g, '<em>$1</em>');
    s = s.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g, function (_, t, u) {
      return '<a href="' + u + '" target="_blank" rel="noopener">' + t + '</a>';
    });
    return s;
  }
  function renderMarkdown(src) {
    if (!src) return '';
    var lines = escapeHtml(src).split('\n');
    var html = '', inCode = false, codeBuf = [], listType = null, para = [];
    function closeList() { if (listType) { html += '</' + listType + '>'; listType = null; } }
    function flushPara() {
      if (para.length) { html += '<p>' + inlineMd(para.join('<br>')) + '</p>'; para = []; }
    }
    for (var i = 0; i < lines.length; i++) {
      var line = lines[i];
      if (/^```/.test(line.trim())) {
        if (inCode) { html += '<pre><code>' + codeBuf.join('\n') + '</code></pre>'; codeBuf = []; inCode = false; }
        else { flushPara(); closeList(); inCode = true; }
        continue;
      }
      if (inCode) { codeBuf.push(line); continue; }
      var h = line.match(/^(#{1,4})\s+(.*)$/);
      if (h) { flushPara(); closeList(); var lv = h[1].length; html += '<h' + lv + '>' + inlineMd(h[2]) + '</h' + lv + '>'; continue; }
      var bq = line.match(/^&gt;\s?(.*)$/);
      if (bq) { flushPara(); closeList(); html += '<blockquote>' + inlineMd(bq[1]) + '</blockquote>'; continue; }
      var ul = line.match(/^[-*]\s+(.*)$/);
      if (ul) { flushPara(); if (listType !== 'ul') { closeList(); html += '<ul>'; listType = 'ul'; } html += '<li>' + inlineMd(ul[1]) + '</li>'; continue; }
      var ol = line.match(/^\d+\.\s+(.*)$/);
      if (ol) { flushPara(); if (listType !== 'ol') { closeList(); html += '<ol>'; listType = 'ol'; } html += '<li>' + inlineMd(ol[1]) + '</li>'; continue; }
      if (line.trim() === '') { flushPara(); closeList(); continue; }
      para.push(line);
    }
    flushPara(); closeList();
    if (inCode) html += '<pre><code>' + codeBuf.join('\n') + '</code></pre>';
    return html;
  }

  /* ---------------- 样式（全部以 cs- 前缀，避免与站点样式冲突） ---------------- */
  var css = [
    '.cs-root{position:fixed;right:24px;bottom:24px;z-index:99999;font-family:inherit;}',
    '.cs-fab{width:56px;height:56px;border-radius:50%;background:' + BRAND + ';color:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;cursor:pointer;box-shadow:0 6px 18px rgba(255,107,53,.4);transition:transform .18s ease;}',
    '.cs-fab:hover{transform:scale(1.06);}',
    '.cs-fab span{font-size:11px;margin-top:1px;}',
    '.cs-panel{position:absolute;right:0;bottom:72px;width:372px;max-width:92vw;height:540px;max-height:78vh;background:#fff;border-radius:14px;box-shadow:0 12px 40px rgba(0,0,0,.18);display:none;flex-direction:column;overflow:hidden;}',
    '.cs-header{height:52px;padding:0 14px;display:flex;align-items:center;justify-content:space-between;background:' + BRAND + ';color:#fff;}',
    '.cs-title{display:flex;align-items:center;gap:8px;font-weight:600;}',
    '.cs-header-actions{display:flex;align-items:center;}',
    '.cs-human{font-size:12px;opacity:.92;cursor:pointer;margin-right:12px;}',
    '.cs-human:hover{opacity:1;text-decoration:underline;}',
    '.cs-close{cursor:pointer;}',
    '.cs-body{flex:1;overflow-y:auto;padding:14px;background:#f7f8fa;}',
    '.cs-msg{display:flex;gap:8px;align-items:flex-start;margin-bottom:12px;}',
    '.cs-msg.user{flex-direction:row-reverse;}',
    '.cs-avatar{width:32px;height:32px;border-radius:50%;flex:0 0 32px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:13px;}',
    '.cs-msg.ai .cs-avatar{background:' + BRAND + ';}',
    '.cs-msg.user .cs-avatar{background:#c8c9cc;}',
    '.cs-bubble{max-width:78%;padding:9px 12px;border-radius:12px;font-size:14px;word-break:break-word;}',
    '.cs-msg.ai .cs-bubble{background:#fff;color:#333;border:1px solid #eee;}',
    '.cs-msg.user .cs-bubble{background:' + BRAND + ';color:#fff;}',
    '.cs-img{max-width:160px;max-height:160px;border-radius:8px;display:block;margin-bottom:4px;}',
    '.cs-text{white-space:pre-wrap;line-height:1.5;}',
    '.cs-typing{display:inline-flex;gap:4px;padding:2px 0;}',
    '.cs-typing span{width:6px;height:6px;border-radius:50%;background:#bbb;animation:cs-blink 1.2s infinite both;}',
    '.cs-typing span:nth-child(2){animation-delay:.2s;}',
    '.cs-typing span:nth-child(3){animation-delay:.4s;}',
    '@keyframes cs-blink{0%,80%,100%{opacity:.3;}40%{opacity:1;}}',
    '.cs-md{line-height:1.6;font-size:14px;word-break:break-word;}',
    '.cs-md p{margin:0 0 8px;}', '.cs-md p:last-child{margin-bottom:0;}',
    '.cs-md h1,.cs-md h2,.cs-md h3,.cs-md h4{margin:6px 0;line-height:1.3;}',
    '.cs-md h1{font-size:18px;}.cs-md h2{font-size:16px;}.cs-md h3{font-size:15px;}',
    '.cs-md ul,.cs-md ol{margin:6px 0;padding-left:20px;}',
    '.cs-md li{margin:2px 0;}',
    '.cs-md code{background:#f2f3f5;padding:1px 4px;border-radius:4px;font-size:13px;}',
    '.cs-md pre{background:#1e1e1e;color:#d4d4d4;padding:10px;border-radius:8px;overflow:auto;}',
    '.cs-md pre code{background:none;padding:0;color:inherit;}',
    '.cs-md blockquote{border-left:3px solid ' + BRAND + ';margin:6px 0;padding:4px 10px;color:#888;background:#fff7f2;}',
    '.cs-md a{color:' + BRAND + ';}',
    '.cs-chips{display:flex;gap:6px;flex-wrap:wrap;padding:0 10px 8px;}',
    '.cs-chip{font-size:12px;color:' + BRAND + ';background:#fff3ec;border:1px solid #ffd9c7;border-radius:14px;padding:3px 10px;cursor:pointer;}',
    '.cs-chip:hover{background:#ffe3d3;}',
    '.cs-input{border-top:1px solid #eee;padding:10px;background:#fff;}',
    '.cs-preview{position:relative;display:inline-block;margin-bottom:8px;}',
    '.cs-preview img{width:64px;height:64px;object-fit:cover;border-radius:8px;}',
    '.cs-del{position:absolute;top:-6px;right:-6px;background:#ff4d4f;color:#fff;border-radius:50%;cursor:pointer;width:18px;height:18px;display:flex;align-items:center;justify-content:center;font-size:12px;}',
    '.cs-input-row{display:flex;align-items:flex-end;gap:8px;}',
    '.cs-input-row textarea{flex:1;border:1px solid #ddd;border-radius:8px;padding:6px 8px;font-size:14px;resize:none;font-family:inherit;outline:none;}',
    '.cs-input-row textarea:focus{border-color:' + BRAND + ';}',
    '.cs-upload{width:40px;height:40px;border:1px dashed #d9d9d9;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#888;cursor:pointer;font-size:18px;}',
    '.cs-upload:hover{color:' + BRAND + ';border-color:' + BRAND + ';}',
    '.cs-btn{border:none;border-radius:8px;padding:0 14px;height:40px;cursor:pointer;font-size:14px;}',
    '.cs-send{background:' + BRAND + ';color:#fff;}',
    '.cs-stop{background:#ff4d4f;color:#fff;}',
    '.cs-btn:disabled{opacity:.6;cursor:not-allowed;}'
  ].join('');
  var styleEl = document.createElement('style');
  styleEl.textContent = css;
  document.head.appendChild(styleEl);

  /* ---------------- DOM ---------------- */
  var root = document.createElement('div');
  root.className = 'cs-root';
  root.innerHTML =
    '<div class="cs-fab" title="客服咨询"><span>客服</span></div>' +
    '<div class="cs-panel">' +
      '<div class="cs-header">' +
        '<div class="cs-title"><span>校园淘 · 智能客服</span></div>' +
        '<div class="cs-header-actions">' +
          '<span class="cs-human">转人工</span>' +
          '<span class="cs-close">✕</span>' +
        '</div>' +
      '</div>' +
      '<div class="cs-body"></div>' +
      '<div class="cs-chips">' +
        '<span class="cs-chip">有哪些在售商品？</span>' +
        '<span class="cs-chip">怎么发布闲置？</span>' +
        '<span class="cs-chip">我的订单在哪看？</span>' +
        '<span class="cs-chip">交易流程是什么？</span>' +
      '</div>' +
      '<div class="cs-input">' +
        '<div class="cs-preview" style="display:none"><img alt="待发送"><span class="cs-del">✕</span></div>' +
        '<div class="cs-input-row">' +
          '<textarea rows="2" placeholder="输入你的问题，或上传图片咨询（Enter 发送 / Shift+Enter 换行）"></textarea>' +
          '<label class="cs-upload" title="上传图片">🖼<input type="file" accept="image/*" hidden></label>' +
          '<button class="cs-btn cs-send">发送</button>' +
        '</div>' +
      '</div>' +
    '</div>';
  document.body.appendChild(root);

  var fab = root.querySelector('.cs-fab');
  var panel = root.querySelector('.cs-panel');
  var body = root.querySelector('.cs-body');
  var textarea = root.querySelector('.cs-input textarea');
  var fileInput = root.querySelector('.cs-upload input');
  var sendBtn = root.querySelector('.cs-send');
  var preview = root.querySelector('.cs-preview');
  var previewImg = preview.querySelector('img');
  var delBtn = preview.querySelector('.cs-del');
  var chips = root.querySelectorAll('.cs-chip');
  var humanBtn = root.querySelector('.cs-human');
  var closeBtn = root.querySelector('.cs-close');

  var open = false, loading = false, es = null, pendingFile = null, previewUrl = null;

  function apiToken() { return (typeof API !== 'undefined' && API.token) ? API.token : ''; }
  function apiUserId() { return (typeof API !== 'undefined' && API.user && API.user.id) ? API.user.id : null; }

  function scrollBottom() { body.scrollTop = body.scrollHeight; }
  function setOpen(v) { open = v; panel.style.display = v ? 'flex' : 'none'; if (v) scrollBottom(); }
  function setLoading(v) {
    loading = v;
    sendBtn.textContent = v ? '停止' : '发送';
    sendBtn.className = 'cs-btn ' + (v ? 'cs-stop' : 'cs-send');
  }

  function addUserMsg(text, image) {
    var wrap = document.createElement('div'); wrap.className = 'cs-msg user';
    var av = document.createElement('div'); av.className = 'cs-avatar'; av.textContent = '你';
    var bub = document.createElement('div'); bub.className = 'cs-bubble';
    if (image) { var im = document.createElement('img'); im.className = 'cs-img'; im.src = image; bub.appendChild(im); }
    var tx = document.createElement('div'); tx.className = 'cs-text'; tx.textContent = text || '';
    bub.appendChild(tx); wrap.appendChild(av); wrap.appendChild(bub); body.appendChild(wrap); scrollBottom();
  }
  function addAiMsg() {
    var wrap = document.createElement('div'); wrap.className = 'cs-msg ai';
    var av = document.createElement('div'); av.className = 'cs-avatar'; av.textContent = '客';
    var bub = document.createElement('div'); bub.className = 'cs-bubble';
    var typing = document.createElement('div'); typing.className = 'cs-typing'; typing.innerHTML = '<span></span><span></span><span></span>';
    var md = document.createElement('div'); md.className = 'cs-md'; md.style.display = 'none';
    bub.appendChild(typing); bub.appendChild(md); wrap.appendChild(av); wrap.appendChild(bub); body.appendChild(wrap); scrollBottom();
    return { typing: typing, md: md, acc: '' };
  }

  function uploadImage(file) {
    return new Promise(function (resolve, reject) {
      var fd = new FormData(); fd.append('file', file);
      var headers = {};
      var tk = apiToken();
      if (tk) headers['Authorization'] = tk;
      fetch('/web/common/upload', { method: 'POST', headers: headers, body: fd })
        .then(function (r) { return r.json(); })
        .then(function (j) { if (j && j.data && j.data.url) resolve(j.data.url); else reject(new Error(j && j.msg ? j.msg : '上传失败')); })
        .catch(function (e) { reject(e); });
    });
  }

  function closeEs() { if (es) { es.close(); es = null; } setLoading(false); }

  function send(textOverride) {
    if (loading) return;
    var text = (textOverride != null ? textOverride : textarea.value).trim();
    if (!text && !pendingFile) return;
    // 发图片需要登录（上传接口要求登录态），未登录直接友好提示
    if (pendingFile && !(typeof API !== 'undefined' && API.isLogin && API.isLogin())) {
      addUserMsg(text || '（图片咨询）', previewUrl);
      var tip = addAiMsg();
      tip.typing.style.display = 'none'; tip.md.style.display = 'block';
      tip.md.innerHTML = '上传图片需要先登录哦～请 <a href="login.html?redirect='
        + encodeURIComponent(location.pathname + location.search)
        + '">前往登录</a>，登录后再发图片咨询。纯文字问题无需登录，随时问我。';
      clearImage(false);
      return;
    }
    // 先备份待发送的图片，避免 clearImage 把它清空后导致图片永远传不上去（之前的 bug）
    var fileToSend = pendingFile;
    var previewSrc = previewUrl;
    addUserMsg(text || '（图片咨询）', previewSrc);
    textarea.value = '';
    setLoading(true);
    clearImage(false); // 仅清掉待发送状态（fileToSend 已备份）

    var ai = addAiMsg();
    var started = false;
    function firstChunk() { if (started) return; started = true; ai.typing.style.display = 'none'; ai.md.style.display = 'block'; }

    var p = Promise.resolve();
    var imageUrl = null;
    if (fileToSend) {
      p = p.then(function () { return uploadImage(fileToSend); }).then(function (u) { imageUrl = u; });
    }
    p.then(function () {
      var params = new URLSearchParams();
      params.set('question', text || '请看看这张图片');
      if (imageUrl) params.set('imageUrl', imageUrl);
      var uid = apiUserId();
      if (uid) params.set('userId', uid);
      es = new EventSource('/api/consultStream?' + params.toString());
      es.onmessage = function (ev) {
        firstChunk();
        ai.acc += ev.data;
        ai.md.innerHTML = renderMarkdown(ai.acc);
        scrollBottom();
      };
      es.onerror = function () {
        if (!ai.acc) ai.md.innerHTML = '抱歉，客服服务连接失败，请稍后重试或转接人工客服。';
        firstChunk();
        closeEs();
      };
    }).catch(function (err) {
      ai.typing.style.display = 'none'; ai.md.style.display = 'block';
      ai.md.innerHTML = '图片上传失败：' + (err && err.message ? err.message : '请稍后重试');
      closeEs();
    });
  }

  function clearImage(revoke) {
    if (revoke && previewUrl && previewUrl.indexOf('blob:') === 0) URL.revokeObjectURL(previewUrl);
    previewUrl = null; pendingFile = null;
    preview.style.display = 'none'; previewImg.removeAttribute('src');
  }

  /* ---------------- 事件 ---------------- */
  fab.addEventListener('click', function () { setOpen(!open); });
  closeBtn.addEventListener('click', function () { setOpen(false); closeEs(); });
  sendBtn.addEventListener('click', function () { if (loading) closeEs(); else send(); });
  delBtn.addEventListener('click', function () { clearImage(true); });
  humanBtn.addEventListener('click', function () {
    if (loading) closeEs();
    addUserMsg('我想转接人工客服');
    var ai = addAiMsg();
    ai.typing.style.display = 'none'; ai.md.style.display = 'block';
    ai.md.innerHTML = '好的，正在为您<b>转接人工客服</b>～请稍候，人工客服会尽快为您服务。如需继续自助咨询，随时告诉我哦。';
  });
  chips.forEach(function (c) {
    c.addEventListener('click', function () { if (!loading) send(c.textContent); });
  });
  textarea.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); }
  });
  fileInput.addEventListener('change', function (e) {
    var f = e.target.files && e.target.files[0];
    if (!f) return;
    pendingFile = f;
    previewUrl = URL.createObjectURL(f);
    previewImg.src = previewUrl;
    preview.style.display = 'inline-block';
    e.target.value = '';
  });

  /* ---------------- 初始化欢迎语 ---------------- */
  (function init() {
    var w = addAiMsg();
    w.typing.style.display = 'none'; w.md.style.display = 'block';
    w.md.innerHTML = '你好，我是校园淘智能客服～可以问我平台使用、商品、订单相关问题，也可以<b>发图片</b>咨询哦。';
  })();
})();
