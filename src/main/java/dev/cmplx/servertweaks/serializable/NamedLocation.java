package dev.cmplx.servertweaks.serializable;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NamedLocation implements Serializable {

	private static final long serialVersionUID = 10L;

	public final double x;
	public final double y;
	public final double z;
	public final UUID dimension;
	public final String name;

	private NamedLocation(double x, double y, double z, UUID worldId, String name) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = worldId;
		this.name = name;
	}

	public Location asLocation() {
		return new Location(Bukkit.getWorld(dimension), x, y, z);
	}

	public static NamedLocation fromLocation(Location l, String name) {
		return new NamedLocation(l.getX(), l.getY(), l.getZ(), l.getWorld().getUID(), name);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NamedLocation nl)) return false;
		return 
			this.x == nl.x &&
			this.y == nl.y &&
			this.z == nl.z &&
			this.name.equals(nl.name) &&
			this.dimension.equals(nl.dimension);
	}
}
