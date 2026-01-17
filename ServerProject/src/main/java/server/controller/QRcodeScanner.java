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

public class QRcodeScanner {

    // --- GENERATOR ---

    /**
     * Generates a QR Code with a default size of 200x200.
     * This matches your email function call: generateQRCodeBase64(String)
     */
    public static String generateQRCodeBase64(String text) {
        return generateQRCodeBase64(text, 200, 200);
    }

    /**
     * Generates a QR Code with custom width and height.
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

    private static String decode(BufferedImage bufferedImage) throws NotFoundException {
        if (bufferedImage == null) return null;
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }

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