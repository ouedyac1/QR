package com.dcsic.qrcode.service;

import com.dcsic.qrcode.dao.repository.PersonRepo;
import com.dcsic.qrcode.dao.repository.UserRepository;
import com.dcsic.qrcode.model.entities.Person;
import com.dcsic.qrcode.model.entities.User;
import com.dcsic.qrcode.model.entities.Institution;
import com.dcsic.qrcode.dao.repository.InstitutionRepo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DiplomaService {

    private final PersonRepo personRepo;
    private final UserRepository userRepository;
    private final InstitutionRepo institutionRepo;

    /**
     * Importe les données depuis un fichier Excel et assigne automatiquement
     * l'institution
     * de l'utilisateur connecté (sauf super admin qui n'a pas d'institution).
     */
    public List<Person> importFromExcel(MultipartFile file, Long institutionId) throws IOException {
        List<Person> savedPersons = new ArrayList<>();

        // 1. Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé en base."));

        Institution institution = currentUser.getInstitution(); // peut être null pour super admin
        if (institution == null && institutionId != null) {
            institution = institutionRepo.findById(institutionId)
                    .orElseThrow(() -> new RuntimeException("Institution non trouvée"));
        }

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // On commence à i=1 pour ignorer l'en-tête
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;

                Person person = new Person();

                // Mapping des colonnes
                person.setNom(getCellValueAsString(row.getCell(0)));
                person.setPrenom(getCellValueAsString(row.getCell(1)));
                person.setEmail(getCellValueAsString(row.getCell(2)));
                person.setTitre(getCellValueAsString(row.getCell(3)));
                person.setDateNaissance(getCellValueAsDate(row.getCell(4)));
                person.setLieuNaissance(getCellValueAsString(row.getCell(5)));
                person.setRegion(getCellValueAsString(row.getCell(6)));
                person.setPays(getCellValueAsString(row.getCell(7)));
                person.setNationalite(getCellValueAsString(row.getCell(8)));
                person.setGroupeSanguin(getCellValueAsString(row.getCell(9)));
                person.setReligion(getCellValueAsString(row.getCell(10)));
                person.setDateService(getCellValueAsDate(row.getCell(11)));
                person.setArmeService(getCellValueAsString(row.getCell(12)));
                person.setGrade(getCellValueAsString(row.getCell(13)));
                person.setDatePromotion(getCellValueAsDate(row.getCell(14)));
                person.setEmailEtudiant(getCellValueAsString(row.getCell(15)));
                person.setAdresse(getCellValueAsString(row.getCell(16)));
                person.setDerniereFonction(getCellValueAsString(row.getCell(17)));
                person.setSituationMatrimoniale(getCellValueAsString(row.getCell(18)));

                // Assigner institution automatiquement
                if (institution != null) {
                    person.setInstitution(institution);
                }

                // Sauvegarder en base
                savedPersons.add(personRepo.save(person));
            }
        }
        return savedPersons;
    }

    /**
     * Génère un QR code pour une personne (format PNG)
     */
    public byte[] generateQRCode(String uniqueCode) throws WriterException, IOException {
        String url = "https://qrcode.defense.bf/verify/" + uniqueCode;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Crée un ZIP contenant les QR codes de toutes les personnes enregistrées
     */
//    public byte[] generateAllQRCodes() throws WriterException, IOException {
//        List<Person> persons = personRepo.findAll();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
//            for (Person person : persons) {
//                byte[] qrCode = generateQRCode(person.getUniqueCode());
//
//                String fileName = person.getUniqueCode() + "_" +
//                        person.getNom() + "_" +
//                        person.getPrenom() + ".png";
//
//                fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
//
//                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(fileName);
//                zos.putNextEntry(entry);
//                zos.write(qrCode);
//                zos.closeEntry();
//            }
//        }
//        return baos.toByteArray();
//    }

    public byte[] generateQRCodesZip(String institutionCode) throws IOException, WriterException {
        List<Person> persons;

        if (institutionCode != null) {
            // On ne récupère que les gens de cette école
            persons = personRepo.findByInstitutionCode(institutionCode);
        } else {
            // On récupère tout le monde
            persons = personRepo.findAll();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Person p : persons) {
                byte[] qrImage = generateQRCode(p.getUniqueCode()); // Votre méthode existante

                ZipEntry entry = new ZipEntry(p.getUniqueCode() + "_" + p.getNom() + ".png");
                zos.putNextEntry(entry);
                zos.write(qrImage);
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        }
    }

    // ------------------ Utilitaires Excel ------------------

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null)
            return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

//    public Optional<Person> verifyDiploma(String uniqueCode) {
//        return personRepo.findByUniqueCode(uniqueCode);
//    }

    public Optional<Person> verifyDiploma(String uniqueCode) {
        return personRepo.findByUniqueCodeWithInstitution(uniqueCode);
    }
    public byte[] generateExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Modèle Import Diplômes");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] columns = {
                    "Nom", "Prénom", "Email", "Titre", "Date Naissance", "Lieu Naissance",
                    "Région", "Pays", "Nationalité", "Groupe Sanguin", "Religion",
                    "Date Service", "Arme Service", "Grade", "Date Promotion",
                    "Email Etudiant", "Adresse", "Dernière Fonction", "Situation Matrimoniale"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
