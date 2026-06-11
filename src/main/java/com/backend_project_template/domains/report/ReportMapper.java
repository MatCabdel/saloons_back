package com.backend_project_template.domains.report;

import com.backend_project_template.domains.report.ReportDTO.ReportedDTO;
import com.backend_project_template.domains.report.ReportDTO.ReporterDTO;
import com.backend_project_template.domains.report.ReportDTO.SaloonInfoDTO;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.user.User;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public ReportDTO toDTO(Report report) {
        User reporter = report.getReporter();
        User reported = report.getReported();
        Saloon saloon = report.getSaloon();

        ReporterDTO reporterDTO = new ReporterDTO(
                reporter.getId(),
                reporter.getUserName(),
                reporter.getImgUrl(),
                reporter.getProfileImageUpdatedAt(),
                reporter.getCity());

        ReportedDTO reportedDTO = new ReportedDTO(
                reported.getId(),
                reported.getUserName(),
                reported.getImgUrl(),
                reported.getProfileImageUpdatedAt(),
                reported.getCity());

        SaloonInfoDTO saloonDTO = null;
        if (saloon != null) {
            saloonDTO = new SaloonInfoDTO(
                    saloon.getId(),
                    saloon.getName(),
                    saloon.getCity());
        }

        return new ReportDTO(
                report.getId(),
                reporterDTO,
                reportedDTO,
                saloonDTO,
                report.getReason(),
                report.getReason().getDisplayName(),
                report.getDescription(),
                report.getStatus(),
                report.getStatus().getDisplayName(),
                report.getCreatedAt(),
                report.getUpdatedAt());
    }
}
