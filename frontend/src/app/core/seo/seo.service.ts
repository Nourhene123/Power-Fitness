import { DOCUMENT } from '@angular/common';
import { Injectable, inject } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { ActivatedRouteSnapshot, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

const DEFAULT_TITLE = 'Power Fitness | Personalized Online Coaching';
const DEFAULT_DESCRIPTION =
  'Personalized online fitness coaching: science-based assessment, custom 12-week workout roadmap, nutrition macros, and progress tracking.';

/**
 * Keeps per-page SEO tags in sync with the router: meta description, canonical URL and the
 * Open Graph / Twitter tags used for link previews. A route sets its description with
 * `data: { description: '…' }`; the page title comes from the route's `title`.
 */
@Injectable({ providedIn: 'root' })
export class SeoService {
  private readonly router = inject(Router);
  private readonly meta = inject(Meta);
  private readonly document = inject(DOCUMENT);

  init(): void {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => this.update(this.router.routerState.snapshot.root));
  }

  private update(root: ActivatedRouteSnapshot): void {
    let route = root;
    while (route.firstChild) {
      route = route.firstChild;
    }

    const title = route.title ?? DEFAULT_TITLE;
    const description = (route.data['description'] as string | undefined) ?? DEFAULT_DESCRIPTION;
    const url = this.document.location.origin + this.router.url.split(/[?#]/)[0];

    this.meta.updateTag({ name: 'description', content: description });
    this.meta.updateTag({ property: 'og:title', content: title });
    this.meta.updateTag({ property: 'og:description', content: description });
    this.meta.updateTag({ property: 'og:url', content: url });
    this.meta.updateTag({ name: 'twitter:title', content: title });
    this.meta.updateTag({ name: 'twitter:description', content: description });
    this.setCanonical(url);
  }

  private setCanonical(url: string): void {
    let link = this.document.head.querySelector<HTMLLinkElement>('link[rel="canonical"]');
    if (!link) {
      link = this.document.createElement('link');
      link.rel = 'canonical';
      this.document.head.appendChild(link);
    }
    link.href = url;
  }
}
