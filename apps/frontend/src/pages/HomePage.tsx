import { Link } from "react-router-dom";

function HomePage() {
  return (
    <section className="home-page">
      <div className="hero">
        <div className="eyebrow">MiniShop Platform</div>
        <h1>A small storefront built for big engineering lessons.</h1>
        <p className="hero-copy">
          Explore users, products, carts, and order history served by MiniShop
          microservices through a single API Gateway.
        </p>

        <div className="hero-actions">
          <Link className="button button-primary" to="/products">
            Browse products
          </Link>
          <Link className="button button-secondary" to="/users">
            View users
          </Link>
          <Link className="button button-secondary" to="/cart">
            Open cart
          </Link>
          <Link className="button button-secondary" to="/orders">
            View orders
          </Link>
        </div>
      </div>

      <div className="feature-grid" aria-label="MiniShop features">
        <article className="feature-card">
          <span className="feature-number">01</span>
          <h2>Microservices</h2>
          <p>
            Independent Java services for users, products, carts, orders, and
            routing.
          </p>
        </article>
        <article className="feature-card">
          <span className="feature-number">02</span>
          <h2>One gateway</h2>
          <p>A single frontend entry point for all MiniShop API traffic.</p>
        </article>
        <article className="feature-card">
          <span className="feature-number">03</span>
          <h2>Built to evolve</h2>
          <p>Ready for containers, observability, Kubernetes, and cloud labs.</p>
        </article>
      </div>
    </section>
  );
}

export default HomePage;
