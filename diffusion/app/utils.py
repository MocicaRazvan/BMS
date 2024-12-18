import io


def process_image_to_bytes(image, idx):
    """Convert a single image to its byte representation."""
    img_byte_arr = io.BytesIO()
    image.save(img_byte_arr, format="PNG")
    img_byte_arr.seek(0)
    return f"image_{idx + 1}.png", img_byte_arr.getvalue()
