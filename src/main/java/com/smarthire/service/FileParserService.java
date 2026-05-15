package com.smarthire.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class FileParserService {

    /**
     * Extract plain text from an uploaded PDF or DOCX file.
     * Uses PDFBox 2.x API (PDDocument.load) which works with InputStream directly.
     */
    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("File has no name. Please upload a valid PDF or DOCX.");
        }

        String lowerName = originalFilename.toLowerCase().trim();

        if (lowerName.endsWith(".pdf")) {
            return extractFromPdf(file.getInputStream());
        } else if (lowerName.endsWith(".docx")) {
            return extractFromDocx(file.getInputStream());
        } else if (lowerName.endsWith(".doc")) {
            throw new IllegalArgumentException(
                "Old .doc format is not supported. Please save your resume as .docx or .pdf.");
        } else {
            throw new IllegalArgumentException(
                "Unsupported file type: " + originalFilename +
                ". Please upload a PDF (.pdf) or Word (.docx) file.");
        }
    }

    /**
     * PDFBox 2.0.x: PDDocument.load(InputStream) works correctly.
     */
    private String extractFromPdf(InputStream inputStream) throws IOException {
        // PDFBox 2.x API — load() accepts InputStream directly
        try (PDDocument document = PDDocument.load(inputStream)) {
            if (document.isEncrypted()) {
                throw new IOException(
                    "The PDF is password-protected. Please upload an unprotected PDF.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            if (text == null || text.trim().isEmpty()) {
                throw new IOException(
                    "No text found in this PDF. It may be a scanned image PDF. " +
                    "Please use the 'Paste Text' tab instead.");
            }
            return text.trim();
        }
    }

    /**
     * Apache POI 5.2.3: reads DOCX paragraphs.
     */
    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            if (paragraphs == null || paragraphs.isEmpty()) {
                throw new IOException("The Word document appears to be empty.");
            }
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text.trim()).append("\n");
                }
            }
            String result = sb.toString().trim();
            if (result.isEmpty()) {
                throw new IOException(
                    "No text found in this Word file. Please check the document content.");
            }
            return result;
        }
    }
}
