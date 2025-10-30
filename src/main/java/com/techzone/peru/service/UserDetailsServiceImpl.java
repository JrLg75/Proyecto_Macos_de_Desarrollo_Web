package com.techzone.peru.service;

import com.techzone.peru.model.entity.Cliente;
import com.techzone.peru.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- ¡Añadir esta!
import com.techzone.peru.model.entity.Rol; // <-- (Asegúrate de que esta también esté)

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Usamos el email como username
        Cliente cliente = clienteRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String password = cliente.getPasswordHash();

        // Esta línea ahora funcionará porque la sesión sigue abierta
        String roleName = cliente.getRol() != null ? cliente.getRol().getNombre() : "ROLE_USER";

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));

        return new User(username, password, authorities);
    }
}


