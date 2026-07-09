import type { KeyboardEvent } from "react";

function isPrintableSingleChar(e: KeyboardEvent<HTMLInputElement>): boolean {
  return e.key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey;
}

/** Blocks digit keys — for name-like fields (letters, spaces, accents, punctuation only). */
export function blockDigits(e: KeyboardEvent<HTMLInputElement>) {
  if (isPrintableSingleChar(e) && /[0-9]/.test(e.key)) {
    e.preventDefault();
  }
}
