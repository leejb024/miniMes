package minimes.master.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import minimes.master.domain.Bom;
import minimes.master.dto.BomMaterialView;

public interface BomRepository extends JpaRepository<Bom, Long> {

	List<Bom> findByUseYnOrderByItemIdAscBomVersionAscBomSeqAsc(String useYn);

	List<Bom> findByBomIdAndUseYnOrderByBomSeqAsc(String bomId, String useYn);

	List<Bom> findByBomIdAndBomVersionAndUseYn(String bomId, String bomVersion, String useYn);

	List<Bom> findByItemIdAndUseYnOrderByBomSeqAsc(String itemId, String useYn);

	boolean existsByItemIdOrMaterialIdOrBomId(String itemId, String materialId, String bomId);

	List<Bom> findByItemIdAndBomVersionAndUseYn(String itemId, String bomVersion, String useYn);

	@Query("""
			select a.materialId as materialId,
			       a.itemId as itemId,
			       a.qty as qty,
			       a.unit as unit,
			       a.bomSeq as bomSeq,
			       b.itemName as itemName
			from Bom a
			left join Item b on a.materialId = b.itemId
			where a.useYn = 'Y'
			  and a.itemId = :itemId
			  and a.bomVersion = :bomVersion
			order by a.bomSeq
			""")
	List<BomMaterialView> findMaterials(@Param("itemId") String itemId, @Param("bomVersion") String bomVersion);
}
