/* TV Akhi: injected at document start in WebView. Same logic as in the GeckoView extension content.js */
//play/pause from the remote (double OK, play/pause key): the video is often in a frame of another site, so a frame
//without a video of its own passes it on to its frames
(function () {
    'use strict';
    if (window.__tvakhiTogglePlayback) return;
    const MESSAGE = 'tvakhi-toggle-playback';
    function mainVideo() {
        let best = null, bestArea = 0;
        for (const v of document.querySelectorAll('video')) {
            const r = v.getBoundingClientRect();
            if (r.width < 200 || r.height < 100) continue; //previews and tiny players
            if (r.width * r.height > bestArea) { best = v; bestArea = r.width * r.height; }
        }
        return best;
    }
    function toggle() {
        const video = mainVideo();
        if (video) {
            if (video.paused || video.ended) {
                const p = video.play();
                if (p && p.catch) p.catch(function () {});
            } else {
                video.pause();
            }
            return;
        }
        for (const f of document.querySelectorAll('iframe, frame')) {
            try { f.contentWindow.postMessage(MESSAGE, '*'); } catch (e) {}
        }
    }
    window.__tvakhiTogglePlayback = toggle;
    window.addEventListener('message', function (e) {
        if (e.data === MESSAGE && e.source === window.parent && window.parent !== window) toggle();
    });
})();

//players in frames can only go fullscreen if their frame allows it: allowed before the frame loads
(function () {
    'use strict';
    if (window.__tvakhiFramesFullscreen) return;
    window.__tvakhiFramesFullscreen = true;
    function allow(frame) {
        if (!frame.hasAttribute('allowfullscreen')) frame.setAttribute('allowfullscreen', '');
        const policy = frame.getAttribute('allow');
        if (policy !== null && !/(^|;)\s*fullscreen/.test(policy)) frame.setAttribute('allow', policy + '; fullscreen');
    }
    new MutationObserver(function (mutations) {
        for (const m of mutations) {
            for (const n of m.addedNodes) {
                if (n.nodeType !== 1) continue;
                if (n.tagName === 'IFRAME') allow(n);
                else if (n.querySelectorAll) n.querySelectorAll('iframe').forEach(allow);
            }
        }
    }).observe(document, { childList: true, subtree: true });
})();

(function () {
    'use strict';
    if (!window.TVBro || window.__tvakhiAutoFullscreen) return;
    window.__tvakhiAutoFullscreen = true;
    //auto fullscreen: when a video starts playing after user action, make its player fullscreen
    function setupAutoFullscreen(isEnabled) {
        const handled = new WeakSet();
        //the player container usually has the same size as the video and holds site's own controls
        function fullscreenTarget(video) {
            const r = video.getBoundingClientRect();
            let target = video;
            for (let p = video.parentElement; p && p !== document.body && p !== document.documentElement; p = p.parentElement) {
                const pr = p.getBoundingClientRect();
                if (pr.width > r.width * 1.15 + 4 || pr.height > r.height * 1.15 + 4) break;
                target = p;
            }
            return target;
        }
        document.addEventListener('play', function (e) {
            const video = e.target;
            if (!(video instanceof HTMLVideoElement) || handled.has(video)) return;
            if (document.fullscreenElement || !isEnabled()) return;
            const r = video.getBoundingClientRect();
            if (r.width < 200 || r.height < 100) return; //previews and tiny players
            handled.add(video);
            try {
                const p = fullscreenTarget(video).requestFullscreen();
                if (p && p.catch) p.catch(function () { handled.delete(video); });
            } catch (err) {
                handled.delete(video);
            }
        }, true);
    }

    setupAutoFullscreen(function () {
        try { return window.TVBro.isAutoFullscreenVideoEnabled(); } catch (e) { return false; }
    });
})();
