package server.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Utility class for generating and scanning QR codes.
 * Uses the Google ZXing library for encoding and decoding operations.
 */
public class QRcodeScanner {

    // --- GENERATOR ---

    /**
     * Generates a QR Code with a default size of 200x200.
     * This matches your email function call: generateQRCodeBase64(String)
     *
     * @param text The text to encode in the QR code.
     * @return The Base64 string representation of the generated QR code.
     */
    public static String generateQRCodeBase64(String text) {
        return generateQRCodeBase64(text, 200, 200);
    }

    /**
     * Generates a QR Code with custom width and height.
     * Encodes the text into a QR code, converts it to a PNG image, and returns the Base64 string.
     *
     * @param text   The text to encode.
     * @param width  The width of the QR code image.
     * @param height The height of the QR code image.
     * @return The Base64 string representation of the QR code, or null if generation fails.
     */
    public static String generateQRCodeBase64(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            System.err.println("Could not generate QR code: " + e.getMessage());
            return null;
        }
    }

    // --- SCANNER (Decoder) ---

    /**
     * Helper method to decode a QR code from a BufferedImage.
     * Uses ZXing MultiFormatReader to interpret the barcode data.
     *
     * @param bufferedImage The image containing the QR code.
     * @return The text encoded in the QR code.
     * @throws NotFoundException If no QR code is found in the image.
     */
    private static String decode(BufferedImage bufferedImage) throws NotFoundException {
        if (bufferedImage == null) return null;
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }

    /**
     * Scans and decodes a QR code from a Base64 encoded string.
     * Handles standard Base64 strings and Data URI schemes (stripping "data:image/...;base64,").
     *
     * @param base64Image The Base64 string of the image.
     * @return The decoded text from the QR code, or null if decoding fails.
     */
    public static String scanQRCodeFromBase64(String base64Image) {
        try {
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bis);
            return decode(bufferedImage);
        } catch (IOException | NotFoundException e) {
            System.err.println("Error decoding QR Code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Scans and decodes a QR code directly from a file path.
     *
     * @param filePath The absolute or relative path to the image file.
     * @return The decoded text from the QR code, or null if decoding fails.
     */
    public static String scanQRCodeFromFile(String filePath) {
        try {
            File file = new File(filePath);
            BufferedImage bufferedImage = ImageIO.read(file);
            return decode(bufferedImage);
        } catch (IOException | NotFoundException e) {
            System.err.println("Error decoding QR Code from File: " + e.getMessage());
            return null;
        }
    }
}