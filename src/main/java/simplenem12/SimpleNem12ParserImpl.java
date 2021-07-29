package simplenem12;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.opencsv.CSVReader;

public class SimpleNem12ParserImpl implements SimpleNem12Parser {

    private String FILE_START = "100";
    private String FILE_END = "900";
    private String METER_READING_START = "200";
    private String METER_READING_VOLUME = "300";
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) throws Exception {

        List<MeterRead> meterReadings = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(simpleNem12File))) {
            String[] dataRowArray;
            ValidityState validState = new ValidityState(false, false);

            MeterRead meterReading = null;
            while ((dataRowArray = reader.readNext()) != null) {
                int colsInRow = dataRowArray.length;
                validateNem12File(dataRowArray, validState, colsInRow);

                String rowType = dataRowArray[0];
                if (colsInRow > 1) { // Normal data
                    if (METER_READING_START.equals(rowType)) {
                        // Create new meter reading and add it to the list.
                        meterReading = new MeterRead(dataRowArray[1], EnergyUnit.KWH);
                        meterReadings.add(meterReading);
                    } else if (METER_READING_VOLUME.equals(rowType)) {
                        // Record the new meter reading.
                        LocalDate localDate = LocalDate.parse(dataRowArray[1], dateFormatter);
                        Quality quality = null;
                        try {
                            quality = Quality.valueOf(dataRowArray[3]);
                        } catch (Exception e) {
                            new NemParserException(
                                    "Invalid Quality indicator " + dataRowArray[3]
                                            + ". Should be either of A or E.");
                        }
                        MeterVolume meterVolume = new MeterVolume(
                                new BigDecimal(dataRowArray[2]), quality);
                        meterReading.appendVolume(localDate, meterVolume);
                    }
                }
            }
        }
        return meterReadings;
    }

    /**
     * This validates the NEM12 file as to the START and END are properly in place. Also validate
     * that the data row (200 / 300) does have 3 columns.
     *
     * @param dataRowArray
     * @param validState
     * @param colsInRow
     */
    private void validateNem12File(String[] dataRowArray, ValidityState validState, int colsInRow)
            throws NemParserException {
        if (!validState.isFileEndFound()) {
            new NemParserException(
                    "Invalid NEM12 file format. Data appears after File end indicator.");
        }
        String dataType = dataRowArray[0];
        if (colsInRow >= 1) {
            if (FILE_START.equals(dataType)) {
                if (colsInRow > 1) {
                    throw new NemParserException(
                            "Invalid NEM12 file format. File line start should be on its own row.");
                }
                validState.setFileStartFound(true);
            } else {
                if (!validState.isFileStartFound()) {
                    new NemParserException("Invalid NEM12 file format. No start of file found");
                } else if (FILE_END.equals(dataType)) {
                    if (colsInRow > 1) {
                        throw new NemParserException(
                                "Invalid NEM12 file format. File line end should be on its own row.");
                    }
                    validState.setFileEndFound(true);
                } else { // This is a data row, so validate that as well
                    if (METER_READING_START.equals(dataType)) {
                        if (colsInRow != 3) {
                            throw new NemParserException(
                                    "Invalid NEM12 file. Expects 3 columns of data for meter read start.");
                        }
                    } else if (METER_READING_VOLUME.equals(dataType)) {
                        if (colsInRow != 4) {
                            throw new NemParserException(
                                    "Invalid NEM12 file. Expects 4 columns of data for meter volume row.");
                        }
                    } else {
                        throw new NemParserException(
                                "Invalid NEM12 file. Unknown data row of type: " + dataType
                                        + ". Expected 200 or 300.");
                    }
                }
            }
        }
    }

    /**
     * Class to track validity
     *
     * @author jjob
     */
    class ValidityState {

        boolean fileStartFound = false;
        boolean fileEndFound = false;

        public ValidityState(boolean fileStartFound, boolean fileEndFound) {
            this.fileStartFound = fileStartFound;
            this.fileEndFound = fileEndFound;
        }

        public boolean isFileStartFound() {
            return fileStartFound;
        }

        public void setFileStartFound(boolean fileStartFound) {
            this.fileStartFound = fileStartFound;
        }

        public boolean isFileEndFound() {
            return fileEndFound;
        }

        public void setFileEndFound(boolean fileEndFound) {
            this.fileEndFound = fileEndFound;
        }
    }
}
