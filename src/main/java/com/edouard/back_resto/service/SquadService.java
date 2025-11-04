package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.Squad;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.model.request.SquadRequest;
import com.edouard.back_resto.repository.SquadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class SquadService {

    private final SquadRepository squadRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public void createSquad(SquadRequest squadRequest) {
        try {
            User user = currentUserService.getCurrentUser();

            if (user.getSquads().size() >= 3) {
                throw new RuntimeException("You can't create/join more than 3 squads");
            }

            Squad squad = new Squad();
            squad.setName(squadRequest.name());
            squad.setCreatedAt(new Date());

            user.addSquad(squad);
            
            squadRepository.save(squad);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error creating squad: " + e.getMessage());
        }
    }

}
