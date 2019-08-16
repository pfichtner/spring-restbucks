package org.springsource.restbucks.core.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
class DocsController {

	@GetMapping(path = "/docs/{rel:^\\w+$}")
	public String resolveDocs(@PathVariable String rel) {
		return "redirect:index.html#".concat(rel);
	}
}
