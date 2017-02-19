package com.cactus.guozy.core.domain.nu;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.cactus.guozy.common.BaseDomain;

@Table(name="goods")
public class Goods extends BaseDomain {

	private static final long serialVersionUID = 1L;

	@Column(name="name")
    private String name;

	@Column(name="price")
    private BigDecimal price;

	@Column(name="need_saler")
    private Boolean needSaler;
	
	@Column(name="pic")
	private String pic;
    
	@Transient
    private List<Category> categories;
    
    public Goods() {}
    public Goods(Long id) {
    	this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getNeedSaler() {
        return needSaler;
    }

    public void setNeedSaler(Boolean needSaler) {
        this.needSaler = needSaler;
    }

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	
	public String getPic() {
		return pic;
	}
	
	public void setPic(String pic) {
		this.pic = pic;
	}
}