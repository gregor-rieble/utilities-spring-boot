package de.gcoding.boot.diagnostics;

public class DiagnosableException extends RuntimeException implements DiagnosisDetailsProvider {
    private final DiagnosisDetails diagnosisDetails;

    public DiagnosableException(String description, String action) {
        this(description, action, null);
    }

    public DiagnosableException(String description, String action, Throwable cause) {
        this(new DiagnosisDetails(description, action), cause);
    }

    public DiagnosableException(DiagnosisDetails diagnosisDetails) {
        this(diagnosisDetails, null);
    }

    public DiagnosableException(DiagnosisDetails diagnosisDetails, Throwable cause) {
        super(diagnosisDetails.description(), cause);
        this.diagnosisDetails = diagnosisDetails;
    }

    @Override
    public DiagnosisDetails getDiagnosisDetails() {
        return diagnosisDetails;
    }
}
