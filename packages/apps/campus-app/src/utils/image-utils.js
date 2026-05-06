export function parseFirstImageUrl(imageUrls) {
  if (!imageUrls || !Array.isArray(imageUrls) || imageUrls.length === 0) {
    return '';
  }
  return imageUrls[0];
}

export function formatImageUrl(url, width, height) {
  if (!url) return '';
  return url;
}