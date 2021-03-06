package com.guilhermefgl.crudsellerclient.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guilhermefgl.crudsellerclient.controller.dto.ClientDto;
import com.guilhermefgl.crudsellerclient.model.Client;
import com.guilhermefgl.crudsellerclient.model.Seller;
import com.guilhermefgl.crudsellerclient.service.ClientService;
import com.guilhermefgl.crudsellerclient.service.SellerService;
import com.guilhermefgl.crudsellerclient.util.Constants;
import com.guilhermefgl.crudsellerclient.util.mapper.ClientMapper;

@RestController
@RequestMapping("/api/client")
public class ClientController {

	@Autowired
	private ClientService clientService;

	@Autowired
	private SellerService sellerService;

	@GetMapping
	public ResponseEntity<List<ClientDto>> list() {
		List<ClientDto> body = clientService.list().stream().map(c -> ClientMapper.toDto(c))
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(body);
	}

	@GetMapping(value = "/seller")
	public ResponseEntity<List<ClientDto>> listWithSeller() {
		List<ClientDto> body = clientService.listClientAndSeller().stream().map(c -> ClientMapper.toDto(c))
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(body);
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<ClientDto> show(@PathVariable("id") Long id) {
		Optional<Client> client = clientService.find(id);

		if (client.isPresent()) {
			return ResponseEntity.status(HttpStatus.OK).body(ClientMapper.toDto(client.get()));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	@PostMapping
	public ResponseEntity<Object> create(@RequestBody ClientDto clientDto, BindingResult result) {
		if (clientDto.getSeller() != null && clientDto.getSeller().getId() != null) {
			Optional<Seller> seller = sellerService.find(clientDto.getSeller().getId());
			if (!seller.isPresent()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.Messages.MSG_SELLER_NOT_FOUND);
			}
		}

		return persist(clientDto, result);
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody ClientDto clientDto,
			BindingResult result) {
		clientDto.setId(id);

		Optional<Client> client = clientService.find(id);
		if (!client.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		if (clientDto.getSeller() != null && clientDto.getSeller().getId() != null) {
			Optional<Seller> seller = sellerService.find(clientDto.getSeller().getId());
			if (!seller.isPresent()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.Messages.MSG_SELLER_NOT_FOUND);
			}
		}

		return persist(clientDto, result);
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> remove(@PathVariable("id") Long id) {
		Optional<Client> client = clientService.find(id);
		if (!client.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		clientService.delete(client.get());

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	private ResponseEntity<Object> persist(ClientDto clientDto, BindingResult result) {
		clientDto.validate(result);
		if (result.hasErrors()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getAllErrors());
		}

		Client client = ClientMapper.toModel(clientDto);
		if (!clientService.isNameUniq(client)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.Messages.MSG_NAME_NOT_UNIQ);
		}

		client = clientService.save(client);

		return ResponseEntity.status(HttpStatus.CREATED).body(ClientMapper.toDto(client));
	}

}
