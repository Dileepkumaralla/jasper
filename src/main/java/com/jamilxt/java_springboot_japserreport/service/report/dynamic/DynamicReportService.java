package com.jamilxt.java_springboot_japserreport.service.report.dynamic;

import com.jamilxt.java_springboot_japserreport.domain.report.dynamic.DynamicReport;
import com.jamilxt.java_springboot_japserreport.domain.report.dynamic.DynamicReportProperties;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

import static com.jamilxt.java_springboot_japserreport.domain.report.ExportType.*;
import static com.jamilxt.java_springboot_japserreport.util.Constant.*;


@Service
public class DynamicReportService {

  public void export(DynamicReport dynamicReport) throws JRException, IOException {
    if (dynamicReport.getExportType() == null)
      throw new RuntimeException(EXPORT_TYPE_PARAMETER_IS_MISSING);

    DynamicReportProperties jasperDynamicReport = dynamicReport.getDynamicReportProperties();
    JasperDesign jasperReportDesign = JRXmlLoader.load(dynamicReport.getInputStream());

    DynamicReportBuilder reportBuilder =
        new DynamicReportBuilder(jasperReportDesign, jasperDynamicReport.getColumnHeaders().size());
    reportBuilder.addDynamicColumns(jasperDynamicReport.getIndexesOfColumnTypeNumber());
    DynamicColumnDataSource dataSource =
        new DynamicColumnDataSource(
            jasperDynamicReport.getColumnHeaders(),
            jasperDynamicReport.getRows(),
            jasperDynamicReport.getSummary());

    HashMap<String, Object> params = new HashMap();
    params.put("title", dynamicReport.getTitle());
    // This param ignore pagination for excel & csv to prevent duplicate ColumnHeader
    if (dynamicReport.getExportType() == EXCEL
        || dynamicReport.getExportType() == CSV)
      params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);

    JasperReport jasperReport = JasperCompileManager.compileReport(jasperReportDesign);
    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
    dynamicReport.exportViaJasperReport(jasperPrint);
  }
}
