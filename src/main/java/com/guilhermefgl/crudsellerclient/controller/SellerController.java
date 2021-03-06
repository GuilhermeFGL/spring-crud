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
import com.guilhermefgl.crudsellerclient.controller.dto.SellerDto;
import com.guilhermefgl.crudsellerclient.model.Seller;
import com.guilhermefgl.crudsellerclient.service.ClientService;
import com.guilhermefgl.crudsellerclient.service.SellerService;
import com.guilhermefgl.crudsellerclient.util.Constants;
import com.guilhermefgl.crudsellerclient.util.mapper.ClientMapper;
import com.guilhermefgl.crudsellerclient.util.mapper.SellerMapper;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

	@Autowired
	private SellerService sellerService;

	@Autowired
	private ClientService clientService;

	@GetMapping
	public ResponseEntity<List<SellerDto>> list() {
		List<SellerDto> body = sellerService.list().stream().map(s -> SellerMapper.toDto(s))
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(body);
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<SellerDto> list(@PathVariable("id") Long id) {
		Optional<Seller> seller = sellerService.find(id);

		if (seller.isPresent()) {
			return ResponseEntity.status(HttpStatus.OK).body(SellerMapper.toDto(seller.get()));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	@PostMapping
	public ResponseEntity<Object> create(@RequestBody SellerDto sellerDto, BindingResult result) {
		return persist(sellerDto, result);
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody SellerDto sellerDto,
			BindingResult result) {
		sellerDto.setId(id);

		Optional<Seller> seller = sellerService.find(id);
		if (!seller.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		return persist(sellerDto, result);
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> remove(@PathVariable("id") Long id) {
		Optional<Seller> seller = sellerService.find(id);
		if (!seller.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		sellerService.delete(seller.get());

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@GetMapping(value = "/{id}/clients")
	public ResponseEntity<List<ClientDto>> listClients(@PathVariable("id") Long id) {
		Optional<Seller> seller = sellerService.find(id);

		if (!seller.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		List<ClientDto> clientsDto = clientService.listClientBySellerId(id).stream().map(c -> ClientMapper.toDto(c))
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(clientsDto);
	}

	private ResponseEntity<Object> persist(SellerDto sellerDto, BindingResult result) {
		sellerDto.validate(result);
		if (result.hasErrors()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getAllErrors());
		}

		Seller seller = SellerMapper.toModel(sellerDto);
		if (!sellerService.isNameUniq(seller)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.Messages.MSG_NAME_NOT_UNIQ);
		}

		seller = sellerService.save(seller);

		return ResponseEntity.status(HttpStatus.CREATED).body(SellerMapper.toDto(seller));
	}

}
