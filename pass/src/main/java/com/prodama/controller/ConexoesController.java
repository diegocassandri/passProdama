package com.prodama.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.prodama.model.Conexao;
import com.prodama.model.ConexaoCliente;
import com.prodama.model.SimNao;
import com.prodama.repository.Clientes;
import com.prodama.repository.ConexaoClientes;
import com.prodama.repository.Conexoes;
import com.prodama.repository.Sistemas;
import com.prodama.repository.TipoConexoes;
import com.prodama.repository.TipoRedes;
import com.prodama.repository.filter.ConexaoFilter;

@Controller
@RequestMapping("/senhas")
public class ConexoesController {

	@Autowired
	private Conexoes conexoes;

	@Autowired
	private Clientes clientes;

	@Autowired
	private TipoRedes tipoRedes;

	@Autowired
	private TipoConexoes tipoConexoes;

	@Autowired
	private Sistemas sistemas;

	@Autowired
	private ConexaoClientes conexaoClientes;

	@GetMapping("/novo")
	public ModelAndView novo(Conexao conexao) {
		
		
		ModelAndView mv = new ModelAndView("cadastro-conexao");
		mv.addObject(conexao);
		mv.addObject("permissoes", SimNao.values());
		mv.addObject("clientes", clientes.findAll());
		mv.addObject("tipoConexoes", tipoConexoes.findAll());
		mv.addObject("tipoRedes", tipoRedes.findAll());
		mv.addObject("todosSistemas", sistemas.findAll());
		try {
			mv.addObject("conexoesClientes", conexaoClientes.findByConexao(conexao));
		} catch (Exception e) {
			List<ConexaoCliente> conexaoCliente = new ArrayList<ConexaoCliente>();
			mv.addObject("conexoesClientes",conexaoCliente);
		}
		
		
		return mv;
	}

	@PostMapping("/novo")
	public ModelAndView salvar(@Valid Conexao conexao, BindingResult result, RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return novo(conexao);
		}

		conexoes.save(conexao);
		attributes.addFlashAttribute("mensagem", "Conexão salva com sucesso!");
		return new ModelAndView("redirect:/senhas/novo");
	}

	@GetMapping("/conexaoSistema/novo")
	public ModelAndView novoConexaoSistema(ConexaoCliente conexaoCliente) {
		ModelAndView mv = new ModelAndView("cadastro-conexao-sistema");
		mv.addObject(conexaoCliente);
		mv.addObject("conexoes", conexoes.findAll());
		mv.addObject("sistemas", sistemas.findAll());
		return mv;
	}
	
	@GetMapping("/conexaoSistema/novo/{codigo}")
	public ModelAndView novoConexaoSistemaAtr(ConexaoCliente conexaoCliente,@PathVariable Long codigo) {
		ModelAndView mv = new ModelAndView("cadastro-conexao-sistema");
		mv.addObject(conexaoCliente);
		mv.addObject("conexoes", conexoes.findOne(codigo));
		mv.addObject("sistemas", sistemas.findAll());
		return mv;
	}

	@PostMapping("/conexaoSistema/novo")
	public ModelAndView salvarConexaoSistema(@Valid ConexaoCliente conexaoCliente, BindingResult result,
			RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return novoConexaoSistema(conexaoCliente);
		}

		conexaoClientes.save(conexaoCliente);
		attributes.addFlashAttribute("mensagem", "sistema salvo com sucesso!");
		return new ModelAndView("redirect:/senhas/" + conexaoCliente.getConexao().getCodigo());
	}

	@GetMapping("/abrir/{codigo}")
	public String abrirConexao(@PathVariable Long codigo) {
		Conexao conexao = conexoes.findOne(codigo);
		String ip = conexao.getIp();
		try {
			Runtime.getRuntime().exec("mstsc.exe /v: " + ip);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/senhas";

	}

	@GetMapping
	public ModelAndView pesquisar(ConexaoFilter conexaoFilter) {
		ModelAndView mv = new ModelAndView("senhas");
		mv.addObject("conexoes", conexoes.findAll());
		/*
		 * mv.addObject("conexoes", conexoes.findByClienteContaining(
		 * Optional.ofNullable(conexaoFilter.getCliente().getNome()).orElse("%")
		 * ));
		 */
		return mv;
	}

	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {
		Conexao conexao = conexoes.findOne(codigo);
		return novo(conexao);
	}

	@DeleteMapping("/{codigo}")
	public String apagar(@PathVariable Long codigo, RedirectAttributes attributes) {
		conexoes.delete(codigo);
		attributes.addFlashAttribute("mensagem", "Conexão removida com sucesso");
		return "redirect:/senhas";
	}
	
	@GetMapping("/conexaoSistema/{codigo}")
	public ModelAndView editarConexao(@PathVariable Long codigo) {
		ConexaoCliente conexao = conexaoClientes.findOne(codigo);
		return novoConexaoSistema(conexao);
	}

	@DeleteMapping("/conexaoSistema/{codigo}")
	public String apagarConexao(@PathVariable Long codigo, RedirectAttributes attributes) {
		conexaoClientes.delete(codigo);
		attributes.addFlashAttribute("mensagem", "Sistema removido com sucesso");
		return "redirect:/senhas";
	}
	

}
