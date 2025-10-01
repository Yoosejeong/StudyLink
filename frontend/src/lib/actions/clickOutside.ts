export function clickOutside(node: HTMLElement) {
  const handleClick = (e: MouseEvent) => {
    if (!node.contains(e.target as Node)) {
      node.dispatchEvent(new CustomEvent('outclick'));
    }
  };
  document.addEventListener('click', handleClick, true);
  return {
    destroy() {
      document.removeEventListener('click', handleClick, true);
    }
  };
}