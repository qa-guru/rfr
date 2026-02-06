package io.student.rangiffler.service.impl;

import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.repository.CountryRepository;
import io.student.rangiffler.model.types.Country;
import io.student.rangiffler.service.api.CountryService;
import io.student.rangiffler.util.BytesAsString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

@Service
public class CountryServiceImpl implements CountryService {

  private final CountryRepository countryRepository;

  @Autowired
  public CountryServiceImpl(CountryRepository countryRepository) {
    this.countryRepository = countryRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Country> getAllCountries() {
    return countryRepository.findAll().stream()
        .map(this::toCountryGql)
        .toList();
  }

  private Country toCountryGql(CountryEntity entity) {
    return Country.newBuilder()
        .code(entity.getCode())
        .name(entity.getName())
        .flag(new BytesAsString(entity.getFlag()).string())
        .build();
  }
}
