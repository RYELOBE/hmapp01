export function parseImageUrl(imageUrls) {
  if (!imageUrls) return [];
  if (typeof imageUrls === 'string') {
    try {
      const parsed = JSON.parse(imageUrls);
      return Array.isArray(parsed) ? parsed : [parsed];
    } catch {
      return imageUrls ? [imageUrls] : [];
    }
  }
  return Array.isArray(imageUrls) ? imageUrls : [];
}

export function parseFirstImageUrl(imageUrls) {
  const urls = parseImageUrl(imageUrls);
  return urls.length > 0 ? urls[0] : '';
}
