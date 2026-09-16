/* enhance.js — UX enhancements (loaded deferred, non-intrusive) */
(function () {
  'use strict';

  var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ---- 1. Skip link for keyboard accessibility ---- */
  if (!document.querySelector('.skip-link')) {
    var skip = document.createElement('a');
    skip.className = 'skip-link';
    skip.href = '#main-content';
    skip.textContent = 'Skip to content';
    document.body.prepend(skip);
    var main = document.querySelector('main, .header__container, .section__container');
    if (main && !document.getElementById('main-content')) main.id = 'main-content';
  }

  var heroCta = document.querySelector('.header__content .btn');

  /* ---- 2. Smooth scroll for in-page anchors ---- */
  document.addEventListener('click', function (e) {
    var a = e.target.closest('a[href^="#"]');
    if (!a || a.getAttribute('href') === '#') return;
    var el = document.getElementById(a.getAttribute('href').slice(1));
    if (el) {
      e.preventDefault();
      el.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'start' });
    }
  });

  /* ---- 4. Sticky mobile call-to-action bar (marketing pages only) ---- */
  var isApp = /\/(auth|coach|admin|client)\//.test(location.pathname);
  var registerLink = document.querySelector('a[href*="register.php"]');
  if (!isApp && registerLink && !document.querySelector('.mobile-cta-bar')) {
    var label = (heroCta && heroCta.textContent.trim()) || 'Build Your Roadmap';
    var bar = document.createElement('div');
    bar.className = 'mobile-cta-bar';
    bar.innerHTML = '<a class="btn" href="' + registerLink.getAttribute('href') +
      '"><i class="ri-flashlight-fill"></i> ' + label + '</a>';
    document.body.appendChild(bar);
  }
})();
