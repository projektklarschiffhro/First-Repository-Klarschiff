package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import de.fraunhofer.igd.klarschiff.service.statistic.StatisticService;

@Controller
public class HomeController {

	@Autowired
	StatisticService statisticService;
	
	@RequestMapping(value="/")
	public String index(ModelMap modelMap, HttpServletRequest request) {
		modelMap.addAttribute("statistic", statisticService.getStatistic());
		return "index";
	}
}
