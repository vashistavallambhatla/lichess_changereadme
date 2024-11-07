import * as licon from 'common/licon';
import * as xhr from 'common/xhr';
import topBar from './topBar';
import { userComplete } from 'common/userComplete';
import { confirm } from 'common/dialog';

export function attachDomHandlers() {
  topBar();

  $('#main-wrap').on('click', '.copy-me__button', function (this: HTMLElement) {
    const showCheckmark = () => {
      $(this).attr('data-icon', licon.Checkmark).removeClass('button-metal');
      setTimeout(() => $(this).attr('data-icon', licon.Clipboard).addClass('button-metal'), 1000);
    };
    $(this.parentElement!.firstElementChild!).each(function (this: any) {
      try {
        navigator.clipboard.writeText(this.value || this.href).then(showCheckmark);
      } catch (e) {
        console.error(e);
      }
    });
    return false;
  });

  $('body').on('click', '.relation-button', function (this: HTMLAnchorElement) {
    const $a = $(this).addClass('processing').css('opacity', 0.3);
    xhr.text(this.href, { method: 'post' }).then(html => {
      if ($a.hasClass('aclose')) $a.hide();
      else if (html.includes('relation-actions')) $a.parent().replaceWith(html);
      else $a.replaceWith(html);
    });
    return false;
  });

  $('.user-autocomplete').each(function (this: HTMLInputElement) {
    const focus = !!this.autofocus;
    const start = () =>
      userComplete({
        input: this,
        friend: !!this.dataset.friend,
        tag: this.dataset.tag as any,
        focus,
      });

    if (focus) start();
    else $(this).one('focus', start);
  });
  console.log('got one!', $('.yes-no-confirm, .ok-cancel-confirm'));
  $('.yes-no-confirm, .ok-cancel-confirm').on('click', async function (this: HTMLElement, e: Event) {
    console.log(e.isTrusted);
    if (!e.isTrusted) return;
    e.preventDefault();
    const [confirmText, cancelText] = this.classList.contains('yes-no-confirm')
      ? [i18n.site.yes, i18n.site.no]
      : [i18n.site.ok, i18n.site.cancel];
    if (await confirm(this.title || 'Confirm this action?', confirmText, cancelText))
      (e.target as HTMLElement)?.click();
  });

  $('#main-wrap').on('click', 'a.bookmark', function (this: HTMLAnchorElement) {
    const t = $(this).toggleClass('bookmarked');
    xhr.text(this.href, { method: 'post' });
    const count = (parseInt(t.text(), 10) || 0) + (t.hasClass('bookmarked') ? 1 : -1);
    t.find('span').html('' + (count > 0 ? count : ''));
    return false;
  });
}
