package com.cycos.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
//public class LotService {
//    @Autowired
//    private LotRepository lotRepository;
//
//    public List<Lot> getAllLots() {
//        return lotRepository.findAll();
//    }
//
//    public Lot getLotByNumber(int number) {
//        return lotRepository.findAll().stream()
//                .filter(lot -> lot.getNumber() == number)
//                .findFirst()
//                .orElse(null);
//    }
//
//    public Lot saveLot(Lot lot) {
//        return lotRepository.save(lot);
//    }
//
//    public void deleteLot(Long id) {
//        lotRepository.deleteById(id);
//    }
//
//}
