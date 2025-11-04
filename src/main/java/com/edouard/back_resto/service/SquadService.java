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

    Integer MAX_SQUAD = 3;

    private final SquadRepository squadRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public void createSquad(SquadRequest squadRequest) {
        try {
            User user = currentUserService.getCurrentUser();

            if (cantJoinOrCreateSquad(user)) {
                throw new RuntimeException("You can't create/join more than 3 squads");
            }

            Squad squad = new Squad();
            squad.setName(squadRequest.name());
            squad.setCreatedAt(new Date());
            squad.setLeader(user);

            user.addSquad(squad);
            
            squadRepository.save(squad);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error creating squad: " + e.getMessage());
        }
    }

    @Transactional
    public void joinSquad(Long squadId) {
        try {
            User user = currentUserService.getCurrentUser();
            Squad squad = squadRepository.findById(squadId).orElseThrow(() -> new RuntimeException("Squad not found"));

            if (cantJoinOrCreateSquad(user)) {
                throw new RuntimeException("You can't create/join more than 3 squads");
            }

            if (squad.getUsers().contains(user)) {
                throw new RuntimeException("You are already in this squad");
            }

            user.addSquad(squad);
            squadRepository.save(squad);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error joining squad: " + e.getMessage());
        }
    }

    @Transactional
    public void leaveSquad(Long squadId) {
        try {
            User user = currentUserService.getCurrentUser();
            Squad squad = squadRepository.findById(squadId).orElseThrow(() -> new RuntimeException("Squad not found"));

            if (!squad.getUsers().contains(user)) {
                throw new RuntimeException("User is not in this squad");
            }

            user.removeSquad(squad);

            if (squad.getUsers().isEmpty()) {
                squadRepository.delete(squad);
            } else {
                squadRepository.save(squad);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error leaving squad: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteSquad(Long squadId) {
        try {
            User user = currentUserService.getCurrentUser();
            Squad squad = squadRepository.findById(squadId).orElseThrow(() -> new RuntimeException("Squad not found"));

            if (!squad.getLeader().equals(user)) {
                throw new RuntimeException("User is not the leader of this squad");
            }

            for (User u : squad.getUsers()) {
                u.removeSquad(squad);
            }

            squadRepository.delete(squad);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error deleting squad: " + e.getMessage());
        }
    }

    private boolean cantJoinOrCreateSquad(User user) {
        return user.getSquads().size() >= MAX_SQUAD;
    }

}
