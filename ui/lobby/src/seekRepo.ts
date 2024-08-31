import LobbyController from './ctrl';
import { Seek } from './interfaces';

function order(a: Seek, b: Seek) {
  return a.rating > b.rating ? -1 : 1;
}

export function sort(ctrl: LobbyController) {
  ctrl.data.seeks.sort(order);
}

export function initAll(ctrl: LobbyController) {
  ctrl.data.seeks.forEach(seek => {
    seek.action = ctrl.me && seek.username === ctrl.me.username ? 'cancelSeek' : 'joinSeek';
    console.log(ctrl.me, seek);
    console.log(ctrl.me?.username, seek.username, seek.action);
  });
  sort(ctrl);
}

export function find(ctrl: LobbyController, id: string) {
  return ctrl.data.seeks.find(s => s.id === id);
}
