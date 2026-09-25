package minimes.master.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.master.domain.Item;

public interface ItemRepository extends JpaRepository<Item, String> {

	List<Item> findAllByOrderByItemIdAsc();
}
