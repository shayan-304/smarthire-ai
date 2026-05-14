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
     * Supports: .pdf, .docx, .doc
     */
    public String extractText(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File has no name.");
        }

        String lowerName = originalFilename.toLowerCase();

        if (lowerName.endsWith(".pdf")) {
            return extractFromPdf(file.getInputStream());
        } else if (lowerName.endsWith(".docx")) {
            return extractFromDocx(file.getInputStream());
        } else {
            throw new IllegalArgumentException(
                "Unsupported file type. Please upload a PDF or DOCX file.");
        }
    }

    private String extractFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.trim().isEmpty()) {
                throw new IOException(
                    "Could not extract text from PDF. " +
                    "Make sure it's a text-based PDF, not a scanned image.");
            }
            return text.trim();
        }
    }

    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            String result = sb.toString().trim();
            if (result.isEmpty()) {
                throw new IOException(
                    "Could not extract text from DOCX file. The document appears to be empty.");
            }
            return result;
        }
    }
}
