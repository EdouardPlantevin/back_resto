package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.Squad;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.model.request.SquadRequest;
import com.edouard.back_resto.repository.SquadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SquadService {

    private static final Integer MAX_SQUAD = 3;
    private static final String ALLOWED = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 8;

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
            squad.setCodeJoin(generateCodeJoin());

            user.addSquad(squad);
            
            squadRepository.save(squad);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error creating squad: " + e.getMessage());
        }
    }

    @Transactional
    public void joinSquad(String codeJoin) {
        try {
            User user = currentUserService.getCurrentUser();
            Squad squad = squadRepository.findByCodeJoin(codeJoin).orElseThrow(() -> new RuntimeException("Squad not found"));

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

            // Créer une copie de la liste pour éviter ConcurrentModificationException
            Set<User> usersToRemove = new HashSet<>(squad.getUsers());
            for (User u : usersToRemove) {
                u.removeSquad(squad);
            }

            squadRepository.delete(squad);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error deleting squad: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    private boolean cantJoinOrCreateSquad(User user) {
        return user.getSquads().size() >= MAX_SQUAD;
    }


    private String generateCodeJoin() {
        StringBuilder sb = new StringBuilder(DEFAULT_LENGTH);
        for (int i = 0; i < DEFAULT_LENGTH; i++) {
            int idx = RANDOM.nextInt(ALLOWED.length());
            sb.append(ALLOWED.charAt(idx));
        }

        if (squadRepository.findByCodeJoin(sb.toString()).isPresent()) {
            return generateCodeJoin();
        }

        return sb.toString();
    }
}
