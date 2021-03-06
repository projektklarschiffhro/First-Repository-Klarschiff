package de.fraunhofer.igd.klarschiff.service.poi;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@Service
public class PoiService {
	public enum Template { vorgangListe, vorgangDelegiertListe }
	String templatePath = "classpath:META-INF/templates/";
	
	Map<Template, String> templates = new HashMap<PoiService.Template, String>();
	
	private HSSFWorkbook readTemplate(Template template) throws IOException {
		String file = templates.get(template);
		if (file==null) file = template.name()+".xls";
		file=templatePath+file;
		Resource resource = new DefaultResourceLoader().getResource(file);
		return new HSSFWorkbook(resource.getInputStream());
	}
	
	@SuppressWarnings("unchecked")
	public HSSFWorkbook createScheet(Template template, List data) throws Exception {
		HSSFWorkbook workbook = readTemplate(template);
		
		switch (template) {
		case vorgangListe:
			{
				Sheet sheet = workbook.getSheetAt(0);			
				int r = 3;
				for(Object[] vorgangData : (List<Object[]>)data)
				{
					Vorgang vorgang = (Vorgang)vorgangData[0];
					Date aenderungsdatum = (Date)vorgangData[1];
					Long unterstuetzer = (Long)vorgangData[2];
					Row row = sheet.createRow(r);
					row.createCell(0).setCellValue(vorgang.getId());
					row.createCell(1).setCellValue(vorgang.getTyp().getText());
					row.createCell(2).setCellValue(vorgang.getDatum());
					row.createCell(3).setCellValue(aenderungsdatum);
					row.createCell(4).setCellValue(vorgang.getKategorie().getParent().getName());
					row.createCell(5).setCellValue(vorgang.getKategorie().getName());
					row.createCell(6).setCellValue(vorgang.getStatus().getText());								
					row.createCell(7).setCellValue(unterstuetzer);	
					row.createCell(8).setCellValue(vorgang.getZustaendigkeit());
					row.createCell(9).setCellValue(vorgang.getZustaendigkeitStatus().getText());
					row.createCell(10).setCellValue(vorgang.getDelegiertAn());
					row.createCell(11).setCellValue(vorgang.getPrioritaet().getText());
					r++;
				}
			}
			break;
		case vorgangDelegiertListe:
			{
				Sheet sheet = workbook.getSheetAt(0);			
				int r = 3;
				for(Vorgang vorgang : (List<Vorgang>)data)
				{
					Row row = sheet.createRow(r);
					row.createCell(0).setCellValue(vorgang.getId());
					row.createCell(1).setCellValue(vorgang.getTyp().getText());
					row.createCell(2).setCellValue(vorgang.getDatum());
					row.createCell(3).setCellValue(vorgang.getKategorie().getParent().getName());
					row.createCell(4).setCellValue(vorgang.getKategorie().getName());
					row.createCell(5).setCellValue(vorgang.getStatus().getText());								
					row.createCell(6).setCellValue(vorgang.getZustaendigkeit());
					row.createCell(7).setCellValue(vorgang.getZustaendigkeitStatus().getText());
					row.createCell(8).setCellValue(vorgang.getDelegiertAn());
					row.createCell(9).setCellValue(vorgang.getPrioritaet().getText());
					r++;
				}
			}
			break;

		default:
			throw new RuntimeException("Template wird nicht unterstützt");
		}
		
		return workbook;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public Map<Template, String> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<Template, String> templates) {
		this.templates = templates;
	}

}
